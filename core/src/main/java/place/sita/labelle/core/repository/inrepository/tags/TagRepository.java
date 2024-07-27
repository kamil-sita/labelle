package place.sita.labelle.core.repository.inrepository.tags;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.jooq.Tables;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.jooq.impl.DSL.row;
import static place.sita.labelle.jooq.Tables.TAG;
import static place.sita.labelle.jooq.Tables.TAG_CATEGORY;

@Component
public class TagRepository {

	// todo this TagRepository assumes that *something* will clean up tags, families after they are no longer needed. Write vacuuming process

	private final DSLContext dslContext;
	private final TagRepositoryProperties tagRepositoryProperties;

	public TagRepository(DSLContext dslContext, TagRepositoryProperties tagRepositoryProperties) {
		this.dslContext = dslContext;
		this.tagRepositoryProperties = tagRepositoryProperties;
	}

	@Transactional
	public void addTags(PersistableImagesTags persistableImagesTags) {
		Set<UUID> images = persistableImagesTags.images();
		if (images.isEmpty()) {
			return;
		}

		Map<UUID, List<UUID>> reposImages = getReposImages(images);

		for (var entry : reposImages.entrySet()) {
			applyChangesToRepo(entry.getKey(), entry.getValue(), persistableImagesTags);
		}
	}

	private void applyChangesToRepo(UUID repoId, List<UUID> imagesIds, PersistableImagesTags persistableImagesTags) {
		Set<String> uniqueCategories = new HashSet<>();
		Set<Tag> uniqueTags = new HashSet<>();
		for (var image : imagesIds) {
			for (var change : persistableImagesTags.tags(image)) {
				uniqueCategories.add(change.category());
				uniqueTags.add(new Tag(change.category(), change.tag()));
			}
		}

		Map<String, UUID> categoryIds = getOrCreateCategoryIds(repoId, uniqueCategories);
		Map<Tag, UUID> tagIds = getOrCreateTagIds(categoryIds, uniqueTags);

		assignTagsToImages(persistableImagesTags, tagIds, imagesIds);
	}

	private Map<UUID, List<UUID>> getReposImages(Set<UUID> images) {
		var results = dslContext.select(Tables.IMAGE.REPOSITORY_ID, Tables.IMAGE.ID)
			.from(Tables.IMAGE)
			.where(Tables.IMAGE.ID.in(images))
			.fetch();

		Map<UUID, List<UUID>> reposImages = new HashMap<>();
		for (var result : results) {
			reposImages.computeIfAbsent(result.value1(), k -> new ArrayList<>()).add(result.value2());
		}

		return reposImages;
	}

	private Map<String, UUID> getOrCreateCategoryIds(UUID actualRepositoryId, Set<String> uniqueCategories) {
		var results = dslContext.select(TAG_CATEGORY.VALUE, TAG_CATEGORY.ID)
			.from(TAG_CATEGORY)
			.where(TAG_CATEGORY.REPOSITORY_ID.eq(actualRepositoryId).and(TAG_CATEGORY.VALUE.in(uniqueCategories)))
			.fetch();

		Map<String, UUID> categoryIds = new HashMap<>();
		for (var result : results) {
			categoryIds.put(result.value1(), result.value2());
		}

		Set<String> missingCategories = new HashSet<>(uniqueCategories);
		missingCategories.removeAll(categoryIds.keySet());

		Map<String, UUID> toInsert = new HashMap<>();
		for (String category : missingCategories) {
			UUID id = UUID.randomUUID();
			categoryIds.put(category, id);
			toInsert.put(category, id);
		}

		if (!toInsert.isEmpty()) {
			var ongoing = dslContext.insertInto(TAG_CATEGORY)
				.columns(TAG_CATEGORY.ID, TAG_CATEGORY.REPOSITORY_ID, TAG_CATEGORY.VALUE);

			for (var entry : toInsert.entrySet()) {
				ongoing = ongoing.values(entry.getValue(), actualRepositoryId, entry.getKey());
			}

			int c = ongoing.execute();
			if (c != toInsert.size()) {
				throw new RuntimeException();
			}
		}

		return categoryIds;
	}

	private Map<Tag, UUID> getOrCreateTagIds(Map<String, UUID> categoryIds, Set<Tag> uniqueTags) {
		record TagViewId(String value, UUID categoryId) { }

		Set<TagViewId> uniqueTagIds = new HashSet<>();
		for (Tag tag : uniqueTags) {
			uniqueTagIds.add(new TagViewId(tag.tag(), categoryIds.get(tag.category())));
		}

		var results = dslContext.select(TAG.VALUE, TAG.TAG_CATEGORY_ID, TAG.ID)
			.from(TAG)
			.where(
				row(TAG.VALUE, TAG.TAG_CATEGORY_ID)
				.in(uniqueTagIds.stream().map(tagViewId -> row(tagViewId.value(), tagViewId.categoryId())).toList())
			)
			.fetch();

		Map<Tag, UUID> tagIds = new HashMap<>();

		for (var result : results) {
			tagIds.put(new Tag(result.value1(), result.value2().toString()), result.value3());
		}

		Set<Tag> missing = new HashSet<>(uniqueTags);
		missing.removeAll(tagIds.keySet());

		Map<Tag, UUID> toInsert = new HashMap<>();
		for (Tag tag : missing) {
			UUID id = UUID.randomUUID();
			tagIds.put(tag, id);
			toInsert.put(tag, id);
		}

		var ongoing = dslContext.insertInto(TAG)
			.columns(TAG.TAG_CATEGORY_ID, TAG.ID, TAG.VALUE);

		for (var entry : toInsert.entrySet()) {
			ongoing = ongoing.values(categoryIds.get(entry.getKey().category()), entry.getValue(), entry.getKey().tag());
		}

		int c = ongoing.execute();
		if (c != toInsert.size()) {
			throw new RuntimeException();
		}

		return tagIds;
	}

	private void assignTagsToImages(PersistableImagesTags persistableImagesTags, Map<Tag, UUID> tagIds, List<UUID> imagesIdsScope) {
		int i = 0;
		while (i < imagesIdsScope.size()) {
			List<UUID> batchOfImages = imagesIdsScope.subList(i, Math.min(i + tagRepositoryProperties.getImageBulkSize(), imagesIdsScope.size()));
			assignTagsToImagesBatch(persistableImagesTags, tagIds, batchOfImages);
			i += tagRepositoryProperties.getImageBulkSize();
		}
	}

	private record ImageTag(UUID imageId, String category, String tag) {

	}

	private void assignTagsToImagesBatch(PersistableImagesTags persistableImagesTags, Map<Tag, UUID> tagIds, List<UUID> batchOfImages) {
		Map<UUID, Set<Tag>> existingTags = new HashMap<>();

		List<ImageTag> imageTags = batchOfImages.stream()
				.mapMulti(new BiConsumer<UUID, Consumer<ImageTag>>() {
					@Override
					public void accept(UUID uuid, Consumer<ImageTag> consumer) {
						persistableImagesTags.tags(uuid).forEach(tag -> consumer.accept(new ImageTag(uuid, tag.category(), tag.tag())));
					}
				})
				.toList();

		dslContext.select(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG, Tables.IMAGE_TAGS.TAG_CATEGORY)
			.from(Tables.IMAGE_TAGS)
			.where(
				row(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG, Tables.IMAGE_TAGS.TAG_CATEGORY)
					.in(imageTags.stream().map(imageTag -> row(imageTag.imageId(), imageTag.tag(), imageTag.category())).toList())
			)
			.fetch()
			.forEach(rr -> {
				UUID imageId = rr.value1();
				String tag = rr.value2();
				String category = rr.value3();
				existingTags.computeIfAbsent(imageId, k -> new HashSet<>()).add(new Tag(tag, category));
			});

		var ongoing = dslContext.insertInto(Tables.TAG_IMAGE)
			.columns(Tables.TAG_IMAGE.TAG_ID, Tables.TAG_IMAGE.IMAGE_ID);

		int toPersist = 0;
		for (var tag : imageTags) {
			UUID imageId = tag.imageId();

			if (existingTags.getOrDefault(imageId, Collections.emptySet()).contains(new Tag(tag.tag(), tag.category()))) {
				continue;
			}

			UUID tagId = tagIds.get(new Tag(tag.category(), tag.tag()));
			ongoing = ongoing.values(tagId, imageId);
			toPersist++;
			if (toPersist > tagRepositoryProperties.getTagBulkSize()) {
				int c = ongoing.execute();
				if (c != toPersist) {
					throw new RuntimeException();
				}
				toPersist = 0;
				ongoing = dslContext.insertInto(Tables.TAG_IMAGE)
					.columns(Tables.TAG_IMAGE.TAG_ID, Tables.TAG_IMAGE.IMAGE_ID);
			}
		}

		if (toPersist > 0) {
			int c = ongoing.execute();
			if (c != toPersist) {
				throw new RuntimeException();
			}
		}
	}

	@Transactional
	public void addTag(UUID imageId, Tag tag) {
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags();
		persistableImagesTags.addTag(imageId, tag);
		addTags(persistableImagesTags);
	}

	@Transactional
	public void deleteTag(UUID imageId, @Nullable UUID repositoryId, Tag tag) {
		UUID tagId = getTagId(imageId, repositoryId, tag.category(), tag.tag());

		dslContext.deleteFrom(Tables.TAG_IMAGE)
			.where(Tables.TAG_IMAGE.TAG_ID.eq(tagId).and(Tables.TAG_IMAGE.IMAGE_ID.eq(imageId)))
			.execute();
	}

	private UUID getTagId(UUID imageId, @Nullable UUID repositoryId, String category, String tag) {
		UUID actualRepositoryId = resolveRepositoryId(imageId, repositoryId);
		UUID categoryId = getCategoryId(actualRepositoryId, category);
		return getTagId(categoryId, tag);
	}

	private UUID getCategoryId(UUID repositoryId, String category) {
		UUID categoryId = resolveCategoryId(repositoryId, category);
		if (categoryId == null) {
			categoryId = putCategory(repositoryId, category);
		}
		return categoryId;
	}

	private UUID resolveRepositoryId(UUID imageId, UUID repositoryId) {
		UUID actualRepositoryId;
		if (repositoryId == null) {
			actualRepositoryId = getRepositoryId(imageId);
		} else {
			actualRepositoryId = repositoryId;
		}
		return actualRepositoryId;
	}

	private UUID getRepositoryId(UUID imageId) {
		var results = dslContext.select(Tables.IMAGE.REPOSITORY_ID)
			.from(Tables.IMAGE)
			.where(Tables.IMAGE.ID.eq(imageId))
			.fetch();

		if (results.size() != 1) {
			throw new RuntimeException();
		}

		return results.get(0).value1();
	}

	private UUID resolveCategoryId(UUID repositoryId, String category) {
		var results = dslContext.select(TAG_CATEGORY.ID)
			.from(TAG_CATEGORY)
			.where(TAG_CATEGORY.REPOSITORY_ID.eq(repositoryId).and(TAG_CATEGORY.VALUE.eq(category)))
			.fetch();


		if (results.size() > 1) {
			throw new RuntimeException();
		}

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0).value1();
	}

	private UUID putCategory(UUID repositoryId, String category) {
		UUID id = UUID.randomUUID();
		int c = dslContext.insertInto(TAG_CATEGORY)
			.columns(TAG_CATEGORY.ID, TAG_CATEGORY.REPOSITORY_ID, TAG_CATEGORY.VALUE)
			.values(id, repositoryId, category)
			.execute();
		if (c != 1) {
			throw new RuntimeException();
		}
		return id;
	}

	private UUID getTagId(UUID categoryId, String tag) {
		UUID tagId = resolveTagId(categoryId, tag);
		if (tagId == null) {
			tagId = createTag(categoryId, tag);
		}
		return tagId;
	}

	private UUID createTag(UUID categoryId, String tag) {
		UUID id = UUID.randomUUID();

		int c = dslContext
			.insertInto(TAG)
			.columns(TAG.TAG_CATEGORY_ID, TAG.ID, TAG.VALUE)
			.values(categoryId, id, tag)
			.execute();

		if (c != 1) {
			throw new RuntimeException();
		}

		return id;
	}

	private UUID resolveTagId(UUID categoryId, String tag) {
		var results = dslContext.select(TAG.ID)
			.from(TAG)
			.where(TAG.TAG_CATEGORY_ID.eq(categoryId).and(TAG.VALUE.eq(tag)))
			.fetch();


		if (results.size() > 1) {
			throw new RuntimeException();
		}

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0).value1();
	}

	public List<Tag> getTags(UUID imageId) {
		return dslContext.select(Tables.IMAGE_TAGS.TAG_CATEGORY, Tables.IMAGE_TAGS.TAG)
			.from(Tables.IMAGE_TAGS)
			.where(Tables.IMAGE_TAGS.IMAGE_ID.eq(imageId))
			.fetch(rr -> new Tag(rr.value1(), rr.value2()));
	}
}
