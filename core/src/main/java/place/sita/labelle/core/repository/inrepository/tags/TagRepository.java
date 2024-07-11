package place.sita.labelle.core.repository.inrepository.tags;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.jooq.Tables;

import javax.annotation.Nullable;
import java.util.*;

import static org.jooq.impl.DSL.row;
import static place.sita.labelle.jooq.Tables.TAG;
import static place.sita.labelle.jooq.Tables.TAG_CATEGORY;

@Component
public class TagRepository {

	// todo this TagRepository assumes that *something* will clean up tags, families after they are no longer needed. Write vacuuming process

	private final DSLContext dslContext;

	public TagRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Transactional
	public void addTags(PersistableImagesTags persistableImagesTags) {
		Set<UUID> images = persistableImagesTags.images();
		if (images.isEmpty()) {
			return;
		}

		UUID anyImage = images.iterator().next();

		// if they're not in the same repository that's like the weirdest usage of this API ever, but might be worth to check if that's the case TODO
		UUID actualRepositoryId = resolveRepositoryId(anyImage, persistableImagesTags.repoId());

		Set<String> uniqueFamilies = persistableImagesTags.categories();
		Map<String, UUID> categoryIds = getOrCreateCategoryIds(actualRepositoryId, uniqueFamilies);

		Set<Tag> uniqueTags = persistableImagesTags.tags();
		Map<Tag, UUID> tagIds = getOrCreateTagIds(categoryIds, uniqueTags);

		assignTagsToImages(persistableImagesTags, tagIds);
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

	private void assignTagsToImages(PersistableImagesTags persistableImagesTags, Map<Tag, UUID> tagIds) {
		Map<UUID, Set<Tag>> existingTags = new HashMap<>();

		dslContext.select(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG, Tables.IMAGE_TAGS.TAG_CATEGORY)
			.from(Tables.IMAGE_TAGS)
			.where(
				row(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG, Tables.IMAGE_TAGS.TAG_CATEGORY)
				.in(persistableImagesTags.imageTags().stream().map(imageTag -> row(imageTag.imageId(), imageTag.tag(), imageTag.category())).toList())
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
		for (var tag : persistableImagesTags.imageTags()) {
			UUID imageId = tag.imageId();

			if (existingTags.getOrDefault(imageId, Collections.emptySet()).contains(new Tag(tag.tag(), tag.category()))) {
				continue;
			}

			UUID tagId = tagIds.get(new Tag(tag.tag(), tag.category()));
			ongoing = ongoing.values(tagId, imageId);
			toPersist++;
		}

		int c = ongoing.execute();
		if (c != toPersist) {
			throw new RuntimeException();
		}
	}

	@Transactional
	public void addTag(UUID imageId, @Nullable UUID repositoryId, String tag, String category) {
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags(repositoryId);
		persistableImagesTags.addTag(imageId, tag, category);
		addTags(persistableImagesTags);
	}


	@Transactional
	public void deleteTag(UUID imageId, @Nullable UUID repositoryId, String tag, String category) {
		UUID tagId = getTagId(imageId, repositoryId, tag, category);

		dslContext.deleteFrom(Tables.TAG_IMAGE)
			.where(Tables.TAG_IMAGE.TAG_ID.eq(tagId).and(Tables.TAG_IMAGE.IMAGE_ID.eq(imageId)))
			.execute();
	}

	private UUID getTagId(UUID imageId, @Nullable UUID repositoryId, String tag, String category) {
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
			.fetch(rr -> new Tag(rr.value2(), rr.value1()));
	}
}
