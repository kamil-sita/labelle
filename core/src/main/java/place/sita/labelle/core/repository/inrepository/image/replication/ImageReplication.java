package place.sita.labelle.core.repository.inrepository.image.replication;

import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep4;
import org.jooq.InsertValuesStep8;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.TagRepository;
import place.sita.labelle.jooq.Tables;
import place.sita.labelle.jooq.tables.Image;
import place.sita.labelle.jooq.tables.records.ImageRecord;
import place.sita.labelle.jooq.tables.records.TagDeltaRecord;

import java.util.*;

import static place.sita.labelle.jooq.Tables.IMAGE;
import static place.sita.labelle.jooq.Tables.REPOSITORY;

@Component
public class ImageReplication {

	private final DSLContext dslContext;
	private final ImageReplicationProperties imageReplicationProperties;
	private final TagRepository tagRepository;

	public ImageReplication(DSLContext dslContext,
	                        ImageReplicationProperties imageReplicationProperties,
	                        TagRepository tagRepository) {
		this.dslContext = dslContext;
		this.imageReplicationProperties = imageReplicationProperties;
		this.tagRepository = tagRepository;
	}

	// duplicate - we want same image, with same tags and same parent, but new persistent ID
	// copy - we're creating a hard copy of an image across repos
	// refer - we're creating a soft copy of an image across repo

	// todo this isn't a perfect abstraction, but it's slightly better than the previous one, it makes it easy to keep
	//  everything in one place.
	@Transactional
	public <ReturnT, ReplicationParamT extends ReplicationParam<ReturnT>> ReturnT execute(ReplicationParamT param) {
		preValidateParams(param);

		Set<UUID> newImageIds = doCopyImages(param);

		fillWithTags(newImageIds, param);

		updateImageDelta(newImageIds, param);

		return prepareResults(newImageIds, param);
	}

	// did you know that you can't have exhaustive switches w/o returning something in Java? This is why this returns Void not void
	private Void preValidateParams(ReplicationParam<?> param) {
		return switch (param) {
			case ReplicationParam.Duplicate duplicate -> preValidateParams(duplicate);
			case ReplicationParam.FillChildRepo fillChildRepo -> preValidateParams(fillChildRepo);
			case ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo -> preValidateParams(hardCopyToNewRepo);
		};
	}

	private Void preValidateParams(ReplicationParam.Duplicate duplicate) {
		boolean exists = dslContext.fetchExists(dslContext.selectFrom(IMAGE).where(IMAGE.ID.eq(duplicate.imageId())));

		if (!exists) {
			throw new ImageReplicationUserException("Image with ID " + duplicate.imageId() + " does not exist");
		}

		return null;
	}

	private Void preValidateParams(ReplicationParam.FillChildRepo fillChildRepo) {
		int parentExistCount = dslContext.fetchCount(dslContext.selectFrom(REPOSITORY).where(REPOSITORY.ID.in(fillChildRepo.parentRepoIds())));

		if (parentExistCount != fillChildRepo.parentRepoIds().size()) {
			throw new ImageReplicationUserException("One or more parent repositories do not exist");
		}

		boolean childExists = dslContext.fetchExists(dslContext.selectFrom(REPOSITORY).where(REPOSITORY.ID.eq(fillChildRepo.childRepoId())));

		if (!childExists) {
			throw new ImageReplicationUserException("Child repository does not exist");
		}

		boolean anyImageExists = dslContext.fetchExists(dslContext.selectFrom(IMAGE).where(IMAGE.REPOSITORY_ID.eq(fillChildRepo.childRepoId())));

		if (anyImageExists) {
			throw new ImageReplicationUserException("Child repository is not empty");
		}

		return null;
	}

	private Void preValidateParams(ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo) {
		boolean sourceExists = dslContext.fetchExists(dslContext.selectFrom(REPOSITORY).where(REPOSITORY.ID.eq(hardCopyToNewRepo.sourceRepoId())));

		if (!sourceExists) {
			throw new ImageReplicationUserException("Source repository does not exist");
		}

		boolean targetExists = dslContext.fetchExists(dslContext.selectFrom(REPOSITORY).where(REPOSITORY.ID.eq(hardCopyToNewRepo.targetRepoId())));

		if (!targetExists) {
			throw new ImageReplicationUserException("Target repository does not exist");
		}

		boolean anyImageExists = dslContext.fetchExists(dslContext.selectFrom(IMAGE).where(IMAGE.REPOSITORY_ID.eq(hardCopyToNewRepo.targetRepoId())));

		if (anyImageExists) {
			throw new ImageReplicationUserException("Target repository is not empty");
		}

		return null;
	}

	private <ReturnT, ReplicationParamT extends ReplicationParam<ReturnT>> Set<UUID> doCopyImages(ReplicationParamT param) {
		return switch (param) {
			case ReplicationParam.Duplicate duplicate -> doDuplicate(duplicate);
			case ReplicationParam.FillChildRepo fillChildRepo -> doFillChildRepo(fillChildRepo);
			case ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo -> doHardCopyToNewRepo(hardCopyToNewRepo);
		};
	}

	private Set<UUID> doDuplicate(ReplicationParam.Duplicate duplicate) {
		var originalImageResult = dslContext
			.selectFrom(IMAGE)
			.where(IMAGE.ID.eq(duplicate.imageId()))
			.fetch();
		var originalImage = originalImageResult.getFirst();

		String newPersistentId = originalImage.get(IMAGE.REFERENCE_ID) + "-" + UUID.randomUUID();
		UUID newImageId = UUID.randomUUID();

		int rc = dslContext
			.insertInto(IMAGE)
			.columns(IMAGE.IMAGE_RESOLVABLE_ID, IMAGE.REPOSITORY_ID, IMAGE.ID, IMAGE.REFERENCE_ID, IMAGE.PARENT_REFERENCE, IMAGE.USE_TAG_DELTA, IMAGE.USE_IMAGE_DELTA, IMAGE.VISIBLE_TO_CHILDREN)
			.values(originalImage.get(IMAGE.IMAGE_RESOLVABLE_ID), originalImage.get(IMAGE.REPOSITORY_ID), newImageId, newPersistentId, originalImage.get(IMAGE.PARENT_REFERENCE), originalImage.get(IMAGE.USE_TAG_DELTA), originalImage.get(IMAGE.USE_IMAGE_DELTA), originalImage.get(IMAGE.VISIBLE_TO_CHILDREN))
			.execute();

		if (rc != 1) {
			throw new ImageReplicationUnexpectedException("Failed to duplicate image");
		}
		return Set.of(newImageId);
	}

	private Set<UUID> doFillChildRepo(ReplicationParam.FillChildRepo fillChildRepo) {
		Set<UUID> createdImageIds = new HashSet<>();

		String lastReference = null;

		var ongoingInsert = createOngoingImageInsert();

		Cursor<ImageRecord> cursor = dslContext
			.selectFrom(IMAGE)
			.where(IMAGE.REPOSITORY_ID.in(fillChildRepo.parentRepoIds()))
			.and(IMAGE.VISIBLE_TO_CHILDREN.isTrue())
			.orderBy(IMAGE.REFERENCE_ID)
			.fetchSize(50)
			.fetchLazy();

		int inserted = 0;

		while (cursor.hasNext()) {
			ImageRecord image = cursor.fetchNext();
			if (image.getReferenceId().equals(lastReference)) {
				continue; // we don't really have a nice logic of merging two different parents with same ID, let's assume they're the same
			}
			lastReference = image.getReferenceId();
			UUID myId = UUID.randomUUID();
			createdImageIds.add(myId);
			ongoingInsert = ongoingInsert
				.values(image.getImageResolvableId(), fillChildRepo.childRepoId(), myId, image.getReferenceId(), image.getReferenceId(), image.getUseTagDelta(), image.getUseImageDelta(), image.getVisibleToChildren());
			inserted++;
			if (inserted > imageReplicationProperties.getTagBulkSize()) {
				ongoingInsert.execute();
				inserted = 0;
				ongoingInsert = createOngoingImageInsert();
			}
		}
		if (inserted > 0) {
			ongoingInsert.execute();
		}

		return createdImageIds;
	}

	private Set<UUID> doHardCopyToNewRepo(ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo) {
		Set<UUID> createdImageIds = new HashSet<>();

		var ongoingInsert = createOngoingImageInsert();

		Cursor<ImageRecord> cursor = dslContext
			.selectFrom(IMAGE)
			.where(IMAGE.REPOSITORY_ID.eq(hardCopyToNewRepo.sourceRepoId()))
			.fetchSize(50)
			.fetchLazy();

		int inserted = 0;

		while (cursor.hasNext()) {
			ImageRecord image = cursor.fetchNext();
			UUID myId = UUID.randomUUID();
			createdImageIds.add(myId);
			ongoingInsert = ongoingInsert
				.values(image.getImageResolvableId(), hardCopyToNewRepo.targetRepoId(), myId, image.getReferenceId(), image.getParentReference(), image.getUseTagDelta(), image.getUseImageDelta(), image.getVisibleToChildren());
			inserted++;
			if (inserted > imageReplicationProperties.getTagBulkSize()) {
				ongoingInsert.execute();
				inserted = 0;
				ongoingInsert = createOngoingImageInsert();
			}
		}
		if (inserted > 0) {
			ongoingInsert.execute();
		}

		return createdImageIds;
	}

	private InsertValuesStep8<ImageRecord, UUID, UUID, UUID, String, String, Boolean, Boolean, Boolean> createOngoingImageInsert() {
		return dslContext.insertInto(IMAGE)
			.columns(IMAGE.IMAGE_RESOLVABLE_ID, IMAGE.REPOSITORY_ID, IMAGE.ID, IMAGE.REFERENCE_ID, IMAGE.PARENT_REFERENCE, IMAGE.USE_TAG_DELTA, IMAGE.USE_IMAGE_DELTA, IMAGE.VISIBLE_TO_CHILDREN);
	}

	private <ReturnT, ReplicationParamT extends ReplicationParam<ReturnT>> Void fillWithTags(Set<UUID> newImageIds, ReplicationParamT param) {
		return switch (param) {
			case ReplicationParam.Duplicate duplicate -> fillDuplicateWithTags(newImageIds, duplicate);
			case ReplicationParam.FillChildRepo fillChildRepo -> fillChildRepoWithTags(newImageIds, fillChildRepo);
			case ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo -> fillHardCopyWithTags(newImageIds, hardCopyToNewRepo);
		};
	}

	private Void fillDuplicateWithTags(Set<UUID> newImageIds, ReplicationParam.Duplicate duplicate) {
		UUID onlyDuplicate = only(newImageIds);

		List<Tag> tags = tagRepository.getTags(duplicate.imageId());
		PersistableImagesTags pit = new PersistableImagesTags();

		for (Tag tag : tags) {
			pit.addTag(onlyDuplicate, tag);
		}

		tagRepository.addTags(pit);

		return null;
	}

	private Void fillChildRepoWithTags(Set<UUID> newImageIds, ReplicationParam.FillChildRepo fillChildRepo) {
		PersistableImagesTags tags = new PersistableImagesTags();

		for (Set<UUID> ids : partition(newImageIds)) {
			// since it's a parent-child relation, let's rely on tag_delta_calc
			dslContext
				.select(Tables.TAG_DELTA_CALC.IMAGE_ID, Tables.TAG_DELTA_CALC.CATEGORY, Tables.TAG_DELTA_CALC.TAG)
				.from(Tables.TAG_DELTA_CALC)
				.where(Tables.TAG_DELTA_CALC.IMAGE_ID.in(ids))
				.fetch()
				.forEach(record -> {
					tags.addTag(record.get(Tables.TAG_DELTA_CALC.IMAGE_ID), new Tag(record.get(Tables.TAG_DELTA_CALC.CATEGORY), record.get(Tables.TAG_DELTA_CALC.TAG)));
				});
		}

		tagRepository.addTags(tags);

		return null;
	}

	private Void fillHardCopyWithTags(Set<UUID> newImageIds, ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo) {
		PersistableImagesTags tags = new PersistableImagesTags();

		for (Set<UUID> ids : partition(newImageIds)) {

			Image origin = IMAGE.as("origin");
			Image copy = IMAGE.as("copy");

			dslContext
				.select(Tables.IMAGE_TAGS.TAG_CATEGORY, Tables.IMAGE_TAGS.TAG, copy.ID)
				.from(Tables.IMAGE_TAGS)
				.join(origin).on(Tables.IMAGE_TAGS.IMAGE_ID.eq(origin.ID))
				.join(copy).on(copy.REFERENCE_ID.eq(origin.REFERENCE_ID))
				.where(copy.ID.in(ids))
				.fetch()
				.forEach(record -> {
					tags.addTag(record.get(copy.ID), new Tag(record.get(Tables.IMAGE_TAGS.TAG_CATEGORY), record.get(Tables.IMAGE_TAGS.TAG)));
				});
		}

		tagRepository.addTags(tags);

		return null;
	}

	private <ReturnT, ReplicationParamT extends ReplicationParam<ReturnT>> Void updateImageDelta(Set<UUID> newImageIds, ReplicationParamT param) {
		return switch (param) {
			case ReplicationParam.Duplicate duplicate -> updateImageDeltaDuplicate(newImageIds, duplicate);
			case ReplicationParam.FillChildRepo fillChildRepo -> updateImageDeltaFillChildRepo(newImageIds, fillChildRepo);
			case ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo -> updateImageDeltaHardCopyToNewRepo(newImageIds, hardCopyToNewRepo);
		};
	}

	private Void updateImageDeltaDuplicate(Set<UUID> newImageIds, ReplicationParam.Duplicate duplicate) {
		UUID onlyDuplicate = only(newImageIds);

		dslContext
			.select(Tables.TAG_DELTA_CALC.IMAGE_ID, Tables.TAG_DELTA_CALC.ADDED, Tables.TAG_DELTA_CALC.CATEGORY, Tables.TAG_DELTA_CALC.TAG)
			.from(Tables.TAG_DELTA_CALC)
			.where(Tables.TAG_DELTA_CALC.IMAGE_ID.eq(duplicate.imageId()))
			.fetch()
			.forEach(record -> {
				dslContext
					.insertInto(Tables.TAG_DELTA)
					.columns(Tables.TAG_DELTA.IMAGE_ID, Tables.TAG_DELTA.ADDS, Tables.TAG_DELTA.CATEGORY, Tables.TAG_DELTA.TAG)
					.values(onlyDuplicate, record.get(Tables.TAG_DELTA_CALC.ADDED), record.get(Tables.TAG_DELTA_CALC.CATEGORY), record.get(Tables.TAG_DELTA_CALC.TAG))
					.execute();
			});

		return null;
	}

	private Void updateImageDeltaFillChildRepo(Set<UUID> newImageIds, ReplicationParam.FillChildRepo fillChildRepo) {

		return null;
	}

	private Void updateImageDeltaHardCopyToNewRepo(Set<UUID> newImageIds, ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo) {
		Image origin = IMAGE.as("origin");
		Image copy = IMAGE.as("copy");

		for (Set<UUID> ids : partition(newImageIds)) {

			var ref = new Object() {
				InsertValuesStep4<TagDeltaRecord, UUID, Boolean, String, String> ongoing =
					dslContext
						.insertInto(Tables.TAG_DELTA)
						.columns(Tables.TAG_DELTA.IMAGE_ID, Tables.TAG_DELTA.ADDS, Tables.TAG_DELTA.CATEGORY, Tables.TAG_DELTA.TAG);
			};

			UUID originRepoId = hardCopyToNewRepo.sourceRepoId();
			// let's copy by referencable ID
			Map<UUID, UUID> copyToOriginId = dslContext
				.select(copy.ID, origin.ID)
				.from(copy)
				.join(origin).on(copy.REFERENCE_ID.eq(origin.REFERENCE_ID))
				.where(copy.ID.in(ids))
				.and(origin.REPOSITORY_ID.eq(originRepoId))
				.fetch()
				.intoMap(copy.ID, origin.ID);

			Set<UUID> originIds = new HashSet<>(copyToOriginId.values());

			dslContext
				.select(Tables.TAG_DELTA.IMAGE_ID, Tables.TAG_DELTA.ADDS, Tables.TAG_DELTA.CATEGORY, Tables.TAG_DELTA.TAG)
				.from(Tables.TAG_DELTA)
				.where(Tables.TAG_DELTA.IMAGE_ID.in(originIds))
				.fetch()
				.forEach(record -> {
					ref.ongoing = ref.ongoing.values(copyToOriginId.get(record.get(Tables.TAG_DELTA.IMAGE_ID)), record.get(Tables.TAG_DELTA.ADDS), record.get(Tables.TAG_DELTA.CATEGORY), record.get(Tables.TAG_DELTA.TAG));
				});

			ref.ongoing.execute();
		}

		return null;
	}

	private <ReturnT, ReplicationParamT extends ReplicationParam<ReturnT>> ReturnT prepareResults(Set<UUID> newImageIds, ReplicationParamT param) {
		return switch (param) {
			case ReplicationParam.Duplicate duplicate -> prepareResultsDuplicate(newImageIds, duplicate);
			case ReplicationParam.FillChildRepo fillChildRepo -> prepareResultsFillChildRepo(newImageIds, fillChildRepo);
			case ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo -> prepareResultsHardCopyToNewRepo(newImageIds, hardCopyToNewRepo);
		};
	}

	private <ReturnT> ReturnT prepareResultsDuplicate(Set<UUID> newImageIds, ReplicationParam.Duplicate duplicate) {
		return (ReturnT) new ReplicationParam.DuplicateImageId(only(newImageIds));
	}

	private <ReturnT> ReturnT prepareResultsFillChildRepo(Set<UUID> newImageIds, ReplicationParam.FillChildRepo fillChildRepo) {
		return null;
	}

	private  <ReturnT> ReturnT prepareResultsHardCopyToNewRepo(Set<UUID> newImageIds, ReplicationParam.HardCopyToNewRepo hardCopyToNewRepo) {
		return null;
	}

	private static UUID only(Set<UUID> newImageIds) {
		return newImageIds.iterator().next();
	}

	//

	private Collection<Set<UUID>> partition(Set<UUID> elements) {
		List<Set<UUID>> partitions = new ArrayList<>();
		Iterator<UUID> iterator = elements.iterator();
		while (iterator.hasNext()) {
			Set<UUID> partition = new HashSet<>();
			for (int i = 0; i < imageReplicationProperties.getTagBulkSize() && iterator.hasNext(); i++) {
				partition.add(iterator.next());
			}
			partitions.add(partition);
		}
		return partitions;
	}

	//

	private static class ImageReplicationUserException extends RuntimeException {
		public ImageReplicationUserException(String message) {
			super(message);
		}
	}

	private static class ImageReplicationUnexpectedException extends RuntimeException {
		public ImageReplicationUnexpectedException(String message) {
			super(message);
		}
	}

}
