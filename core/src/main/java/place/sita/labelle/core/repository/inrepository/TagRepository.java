package place.sita.labelle.core.repository.inrepository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.jooq.Tables;

import javax.annotation.Nullable;
import java.util.*;

import static org.jooq.impl.DSL.row;

@Component
public class TagRepository {

	// todo this TagRepository assumes that *something* will clean up tags, families after they are no longer needed. Write vacuuming process

	private final DSLContext dslContext;

	public TagRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Transactional
	public void invalidateCaches() {
		// todo due to transactionality issues caching was removed; re-add it
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

		Set<String> uniqueFamilies = persistableImagesTags.families();
		Map<String, UUID> familyIds = getOrCreateFamilyIds(actualRepositoryId, uniqueFamilies);

		Set<TagView> uniqueTags = persistableImagesTags.tags();
		Map<TagView, UUID> tagIds = getOrCreateTagIds(familyIds, uniqueTags);

		assignTagsToImages(persistableImagesTags, tagIds);
	}

	private Map<String, UUID> getOrCreateFamilyIds(UUID actualRepositoryId, Set<String> uniqueFamilies) {
		var results = dslContext.select(Tables.TAG_SRC.VALUE, Tables.TAG_SRC.ID)
			.from(Tables.TAG_SRC)
			.where(Tables.TAG_SRC.REPOSITORY_ID.eq(actualRepositoryId).and(Tables.TAG_SRC.VALUE.in(uniqueFamilies)))
			.fetch();

		Map<String, UUID> familyIds = new HashMap<>();
		for (var result : results) {
			familyIds.put(result.value1(), result.value2());
		}

		Set<String> missing = new HashSet<>(uniqueFamilies);
		missing.removeAll(familyIds.keySet());

		Map<String, UUID> toInsert = new HashMap<>();
		for (String family : missing) {
			UUID id = UUID.randomUUID();
			familyIds.put(family, id);
			toInsert.put(family, id);
		}

		var ongoing = dslContext.insertInto(Tables.TAG_SRC)
			.columns(Tables.TAG_SRC.ID, Tables.TAG_SRC.REPOSITORY_ID, Tables.TAG_SRC.VALUE);

		for (var entry : toInsert.entrySet()) {
			ongoing = ongoing.values(entry.getValue(), actualRepositoryId, entry.getKey());
		}

		int c = ongoing.execute();
		if (c != toInsert.size()) {
			throw new RuntimeException();
		}

		return familyIds;
	}

	private Map<TagView, UUID> getOrCreateTagIds(Map<String, UUID> familyIds, Set<TagView> uniqueTags) {
		record TagViewId(String value, UUID familyId) { }

		Set<TagViewId> uniqueTagIds = new HashSet<>();
		for (TagView tag : uniqueTags) {
			uniqueTagIds.add(new TagViewId(tag.value(), familyIds.get(tag.family())));
		}

		var results = dslContext.select(Tables.TAG.VALUE, Tables.TAG.TAG_SRC_ID, Tables.TAG.ID)
			.from(Tables.TAG)
			.where(
				row(Tables.TAG.VALUE, Tables.TAG.TAG_SRC_ID)
				.in(uniqueTagIds.stream().map(tagViewId -> row(tagViewId.value(), tagViewId.familyId())).toList())
			)
			.fetch();

		Map<TagView, UUID> tagIds = new HashMap<>();

		for (var result : results) {
			tagIds.put(new TagView(result.value1(), result.value2().toString()), result.value3());
		}

		Set<TagView> missing = new HashSet<>(uniqueTags);
		missing.removeAll(tagIds.keySet());

		Map<TagView, UUID> toInsert = new HashMap<>();
		for (TagView tag : missing) {
			UUID id = UUID.randomUUID();
			tagIds.put(tag, id);
			toInsert.put(tag, id);
		}

		var ongoing = dslContext.insertInto(Tables.TAG)
			.columns(Tables.TAG.TAG_SRC_ID, Tables.TAG.ID, Tables.TAG.VALUE);

		for (var entry : toInsert.entrySet()) {
			ongoing = ongoing.values(familyIds.get(entry.getKey().family()), entry.getValue(), entry.getKey().value());
		}

		int c = ongoing.execute();
		if (c != toInsert.size()) {
			throw new RuntimeException();
		}

		return tagIds;
	}

	private void assignTagsToImages(PersistableImagesTags persistableImagesTags, Map<TagView, UUID> tagIds) {
		Map<UUID, Set<TagView>> existingTags = new HashMap<>();

		dslContext.select(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG_VALUE, Tables.IMAGE_TAGS.TAG_FAMILY)
			.from(Tables.IMAGE_TAGS)
			.where(
				row(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG_VALUE, Tables.IMAGE_TAGS.TAG_FAMILY)
				.in(persistableImagesTags.imageTags().stream().map(imageTag -> row(imageTag.imageId(), imageTag.tag(), imageTag.family())).toList())
			)
			.fetch()
			.stream()
			.forEach(rr -> {
				UUID imageId = rr.value1();
				String tag = rr.value2();
				String family = rr.value3();
				existingTags.computeIfAbsent(imageId, k -> new HashSet<>()).add(new TagView(tag, family));
			});

		var ongoing = dslContext.insertInto(Tables.TAG_IMAGE)
			.columns(Tables.TAG_IMAGE.TAG_ID, Tables.TAG_IMAGE.IMAGE_ID);

		int toPersist = 0;
		for (var tag : persistableImagesTags.imageTags()) {
			UUID tagId = tagIds.get(new TagView(tag.tag(), tag.family()));
			UUID imageId = tag.imageId();

			if (existingTags.getOrDefault(imageId, Collections.emptySet()).contains(new TagView(tag.tag(), tag.family()))) {
				return;
			}

			ongoing = ongoing.values(tagId, imageId);
			toPersist++;
		}

		int c = ongoing.execute();
		if (c != toPersist) {
			throw new RuntimeException();
		}
	}

	@Transactional
	public void addTag(UUID imageId, @Nullable UUID repositoryId, String tag, String family) {
		PersistableImagesTags persistableImagesTags = new PersistableImagesTags(repositoryId);
		persistableImagesTags.addTag(imageId, tag, family);
		addTags(persistableImagesTags);
	}


	@Transactional
	public void deleteTag(UUID imageId, @Nullable UUID repositoryId, String tag, String family) {
		UUID tagId = getTagId(imageId, repositoryId, tag, family);

		dslContext.deleteFrom(Tables.TAG_IMAGE)
			.where(Tables.TAG_IMAGE.TAG_ID.eq(tagId).and(Tables.TAG_IMAGE.IMAGE_ID.eq(imageId)))
			.execute();
	}

	private UUID getTagId(UUID imageId, @Nullable UUID repositoryId, String tag, String family) {
		UUID actualRepositoryId = resolveRepositoryId(imageId, repositoryId);
		UUID familyId = getFamilyId(actualRepositoryId, family);
		return getTagId(familyId, tag);
	}

	private UUID getFamilyId(UUID repositoryId, String family) {
		UUID familyId = resolveFamilyId(repositoryId, family);
		if (familyId == null) {
			familyId = putFamily(repositoryId, family);
		}
		return familyId;
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

	private UUID resolveFamilyId(UUID repositoryId, String family) {
		var results = dslContext.select(Tables.TAG_SRC.ID)
			.from(Tables.TAG_SRC)
			.where(Tables.TAG_SRC.REPOSITORY_ID.eq(repositoryId).and(Tables.TAG_SRC.VALUE.eq(family)))
			.fetch();


		if (results.size() > 1) {
			throw new RuntimeException();
		}

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0).value1();
	}

	private UUID putFamily(UUID repositoryId, String family) {
		UUID id = UUID.randomUUID();
		int c = dslContext.insertInto(Tables.TAG_SRC)
			.columns(Tables.TAG_SRC.ID, Tables.TAG_SRC.REPOSITORY_ID, Tables.TAG_SRC.VALUE)
			.values(id, repositoryId, family)
			.execute();
		if (c != 1) {
			throw new RuntimeException();
		}
		return id;
	}

	private UUID getTagId(UUID familyId, String tag) {
		UUID tagId = resolveTagId(familyId, tag);
		if (tagId == null) {
			tagId = createTag(familyId, tag);
		}
		return tagId;
	}

	private UUID createTag(UUID familyId, String tag) {
		UUID id = UUID.randomUUID();

		int c = dslContext
			.insertInto(Tables.TAG)
			.columns(Tables.TAG.TAG_SRC_ID, Tables.TAG.ID, Tables.TAG.VALUE)
			.values(familyId, id, tag)
			.execute();

		if (c != 1) {
			throw new RuntimeException();
		}

		return id;
	}

	private UUID resolveTagId(UUID familyId, String tag) {
		var results = dslContext.select(Tables.TAG.ID)
			.from(Tables.TAG)
			.where(Tables.TAG.TAG_SRC_ID.eq(familyId).and(Tables.TAG.VALUE.eq(tag)))
			.fetch();


		if (results.size() > 1) {
			throw new RuntimeException();
		}

		if (results.isEmpty()) {
			return null;
		}

		return results.get(0).value1();
	}

	public List<TagView> getTags(UUID imageId) {
		return dslContext.select(Tables.IMAGE_TAGS.TAG_FAMILY, Tables.IMAGE_TAGS.TAG_VALUE)
			.from(Tables.IMAGE_TAGS)
			.where(Tables.IMAGE_TAGS.IMAGE_ID.eq(imageId))
			.fetch(rr -> new TagView(rr.value2(), rr.value1()));
	}

	public record TagView(String value, String family) {

	}
}
