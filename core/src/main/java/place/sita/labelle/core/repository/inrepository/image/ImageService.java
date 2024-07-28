package place.sita.labelle.core.repository.inrepository.image;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.core.repository.RootRepository;
import place.sita.labelle.core.repository.inrepository.Ids;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.TagRepository;
import place.sita.labelle.core.utils.Result3;
import place.sita.labelle.datasource.cross.PreprocessableIdDataSourceWithRemoval;
import place.sita.labelle.jooq.Tables;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.TAG_DELTA;
import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Service
public class ImageService {

	private final DSLContext dslContext;
	private final ImageRepository imageRepository;
	private final RootRepository rootRepository;
	private final TagRepository tagRepository;

	public ImageService(DSLContext dslContext,
	                    ImageRepository imageRepository,
	                    RootRepository rootRepository,
	                    TagRepository tagRepository) {
		this.dslContext = dslContext;
		this.imageRepository = imageRepository;
		this.rootRepository = rootRepository;
		this.tagRepository = tagRepository;
	}


	public <Self extends PreprocessableIdDataSourceWithRemoval<UUID, ImageResponse, ImageRepository.FilteringApi<Self>, Self>> Self images() {
		return imageRepository.images();
	}

	@Transactional
	public Result3<ImageResponse, InRepositoryService.DoesNotMatchAnyRoot, InRepositoryService.InsertFailedUnexpected> addImage(UUID repoId, File file) {
		String fileDir = file.getPath();
		return addImage(repoId, fileDir);
	}

	@Transactional
	public Result3<ImageResponse, InRepositoryService.DoesNotMatchAnyRoot, InRepositoryService.InsertFailedUnexpected> addImage(UUID repoId, String absolutePath) {
		List<Root> roots = rootRepository.getRoots();
		Root r = findRoot(roots, absolutePath);
		if (r == null) {
			return Result3.failure1(new InRepositoryService.DoesNotMatchAnyRoot());
		}
		// todo transaction
		String relPath = subtr(absolutePath, r.directory());
		UUID imageFileId = UUID.randomUUID();
		dslContext.insertInto(Tables.IMAGE_FILE)
			.columns(Tables.IMAGE_FILE.ID, Tables.IMAGE_FILE.RELATIVE_DIR, Tables.IMAGE_FILE.ROOT_ID)
			.values(imageFileId, relPath, r.id())
			.execute();
		UUID imageResolvableId = UUID.randomUUID();
		dslContext.insertInto(Tables.IMAGE_RESOLVABLE)
			.columns(Tables.IMAGE_RESOLVABLE.ID, Tables.IMAGE_RESOLVABLE.IMAGE_FILE_ID, Tables.IMAGE_RESOLVABLE.SYNTHETIC)
			.values(imageResolvableId, imageFileId, false)
			.execute();
		UUID imageInRepoId = UUID.randomUUID();
		String reference = UUID.randomUUID().toString(); // todo can we generate something more friendly?
		dslContext.insertInto(Tables.IMAGE)
			.columns(Tables.IMAGE.ID, Tables.IMAGE.IMAGE_RESOLVABLE_ID, Tables.IMAGE.REPOSITORY_ID, Tables.IMAGE.REFERENCE_ID)
			.values(imageInRepoId, imageResolvableId, repoId, reference)
			.execute();

		ImageResponse image = new ImageResponse(imageInRepoId, r.directory(), relPath);
		return Result3.success(image);
	}

	/**
	 * Copies this image definition to another repository.
	 *
	 * Note that this should not be used for children-parent repositories, but only for clones of repositories. See {@link ImageService#referImage(UUID, UUID, String)}
	 */
	@Transactional
	public UUID copyImage(UUID newRepoId, UUID originalImageId) {
		return copyOrRefer(newRepoId, originalImageId, CopyOrRefer.COPY, null);
	}

	@Transactional
	public UUID referImage(UUID newRepoId, UUID originalImageId, String persistentId) {
		return copyOrRefer(newRepoId, originalImageId, CopyOrRefer.REFER, persistentId);
	}

	@Transactional
	public UpdateIdsResult updateIds(UUID imageId, String persistentId, String parentPersistentId, boolean isVisibleToChildren) {
		UUID imageRepoId = JqRepo.fetchOne(() ->
			dslContext
				.select(IMAGE.REPOSITORY_ID)
				.from(IMAGE)
				.where(IMAGE.ID.eq(imageId))
				.fetch()
		);
		boolean exists = dslContext.fetchExists(IMAGE,
			IMAGE.REFERENCE_ID.eq(persistentId)
				.and(IMAGE.REPOSITORY_ID.eq(imageRepoId))
				.and(IMAGE.ID.ne(imageId))
		);
		if (exists) {
			return new UpdateIdsResult.IdReuse();
		}

		setPersistentId(imageId, persistentId);
		setVisibility(imageId, isVisibleToChildren);
		setParentPersistentId(imageId, parentPersistentId);
		return new UpdateIdsResult.Success();
	}

	public sealed interface UpdateIdsResult {
		record Success() implements UpdateIdsResult {}
		record IdReuse() implements UpdateIdsResult {}
	}

	@Transactional // todo make this one throw if something goes wrong - unless it already does?
	public void setPersistentId(UUID imageId, String persistentId) {
		dslContext.update(IMAGE)
			.set(IMAGE.REFERENCE_ID, persistentId)
			.where(IMAGE.ID.eq(imageId))
			.execute();
	}

	@Transactional
	public void setParentPersistentId(UUID imageId, String parentPersistentId) {
		dslContext.update(IMAGE)
			.set(IMAGE.PARENT_REFERENCE, parentPersistentId)
			.where(IMAGE.ID.eq(imageId))
			.execute();
	}

	@Transactional(readOnly = true)
	public Ids getIds(UUID imageId) {
		return dslContext
			.select(IMAGE.ID, IMAGE.REFERENCE_ID, IMAGE.PARENT_REFERENCE, IMAGE.VISIBLE_TO_CHILDREN)
			.from(IMAGE)
			.where(IMAGE.ID.equal(imageId))
			.fetchOne(rr -> {
				return new Ids(rr.value1(), rr.value2(), rr.value3(), rr.value4());
			});
	}

	@Transactional
	public void setVisibility(UUID imageId, boolean value) {
		dslContext.update(IMAGE)
			.set(IMAGE.VISIBLE_TO_CHILDREN, value)
			.where(IMAGE.ID.eq(imageId))
			.execute();
	}

	@Transactional
	public UUID duplicateImage(UUID selectedImageId) {
		// todo consider rewriting this to be event-based.
		UUID newId = UUID.randomUUID();
		UUID newReference = UUID.randomUUID();

		var originalImage = dslContext.select(IMAGE.ID, IMAGE.IMAGE_RESOLVABLE_ID, IMAGE.REPOSITORY_ID, IMAGE.REFERENCE_ID, IMAGE.PARENT_REFERENCE, IMAGE.VISIBLE_TO_CHILDREN)
			.from(IMAGE)
			.where(IMAGE.ID.eq(selectedImageId))
			.fetch()
			.getFirst();


		dslContext.insertInto(IMAGE)
			.columns(IMAGE.ID, IMAGE.IMAGE_RESOLVABLE_ID, IMAGE.REPOSITORY_ID, IMAGE.REFERENCE_ID, IMAGE.PARENT_REFERENCE, IMAGE.VISIBLE_TO_CHILDREN)
			.values(newId, originalImage.value2(), originalImage.value3(), newReference.toString(), originalImage.value5(), originalImage.value6())
			.execute();

		PersistableImagesTags pit = new PersistableImagesTags();
		tagRepository.getTags(selectedImageId).forEach(tag -> pit.addTag(newId, tag));
		tagRepository.addTags(pit);

		var originalDelta = dslContext.select(TAG_DELTA.IMAGE_ID, TAG_DELTA.ADDS, TAG_DELTA.CATEGORY, TAG_DELTA.TAG)
			.from(TAG_DELTA)
			.where(TAG_DELTA.IMAGE_ID.eq(selectedImageId))
			.fetch();

		var ongoingTagDelta =
			dslContext.insertInto(TAG_DELTA)
				.columns(TAG_DELTA.IMAGE_ID, TAG_DELTA.ADDS, TAG_DELTA.CATEGORY, TAG_DELTA.TAG);

		for (var delta : originalDelta) {
			ongoingTagDelta = ongoingTagDelta.values(newId, delta.value2(), delta.value3(), delta.value4());
		}

		ongoingTagDelta.execute();

		// todo image delta

		return newId;
	}

	private enum CopyOrRefer {
		COPY,
		REFER;
	}

	private UUID copyOrRefer(UUID newRepoId, UUID originalImageId, CopyOrRefer copyOrRefer, String persistentId) {
		// this operation makes absolutely no sense if we are copying from the same repo, or if this image exists here. Let's check for that.
		UUID underlyingImageResolvable = JqRepo.fetchOne(() ->
			dslContext
				.select(IMAGE.IMAGE_RESOLVABLE_ID)
				.from(IMAGE)
				.where(IMAGE.ID.eq(originalImageId))
				.fetch()
		);

		boolean thisImageExistsInCurrentRepo =
			dslContext.fetchExists(
				dslContext
					.select(IMAGE.ID)
					.from(IMAGE)
					.where(IMAGE.REPOSITORY_ID.eq(newRepoId).and(IMAGE.IMAGE_RESOLVABLE_ID.eq(underlyingImageResolvable)))
			);

		if (thisImageExistsInCurrentRepo) {
			throw new RuntimeException("Image already exists in this repository.");
		}

		UUID newImageId = UUID.randomUUID();

		String referenceId;
		if (persistentId == null) {
			referenceId = JqRepo.fetchOne(() ->
				dslContext
					.select(IMAGE.REFERENCE_ID)
					.from(IMAGE)
					.where(IMAGE.ID.eq(originalImageId))
					.fetch()
			);
		} else {
			referenceId = persistentId;
		}

		String parentReferenceId;
		if (copyOrRefer == CopyOrRefer.COPY) {
			parentReferenceId = JqRepo.fetchOne(() ->
				dslContext
					.select(IMAGE.PARENT_REFERENCE)
					.from(IMAGE)
					.where(IMAGE.ID.eq(originalImageId))
					.fetch()
			);
		} else {
			parentReferenceId = referenceId;
		}

		// todo we can probably do with less queries. And better copying, not one by one.

		JqRepo.insertOne(() ->
			dslContext.insertInto(IMAGE)
				.columns(IMAGE.ID, IMAGE.IMAGE_RESOLVABLE_ID, IMAGE.REPOSITORY_ID, IMAGE.REFERENCE_ID, IMAGE.PARENT_REFERENCE)
				.values(newImageId, underlyingImageResolvable, newRepoId, referenceId, parentReferenceId)
				.execute()
		);

		return newImageId;
	}

	public ImagePtr getImagePtr(UUID imageId) {
		return dslContext
			.select(IMAGE.ID, IMAGE.imageResolvable().imageFile().RELATIVE_DIR, IMAGE.imageResolvable().imageFile().root().ROOT_DIR)
			.from(IMAGE)
			.where(IMAGE.ID.equal(imageId))
			.fetch()
			.map(rr -> {
				return new ImageResponse(rr.value1(), rr.value3(), rr.value2()).toPtr();
			})
			.get(0);
	}

	/**
	 * This is currently mostly a test method, and will fail most executions against actual code,
	 * but long term it should be a supported operation to add an (non-empty) synthethic image to a repository.
	 */
	@Transactional
	public UUID addEmptySyntheticImage(UUID repoId) {
		UUID imageResolvableId = UUID.randomUUID();
		dslContext.insertInto(Tables.IMAGE_RESOLVABLE)
			.columns(Tables.IMAGE_RESOLVABLE.ID, Tables.IMAGE_RESOLVABLE.IMAGE_FILE_ID, Tables.IMAGE_RESOLVABLE.SYNTHETIC)
			.values(imageResolvableId, null, true)
			.execute();
		UUID imageInRepoId = UUID.randomUUID();
		UUID referenceId = UUID.randomUUID();
		dslContext.insertInto(Tables.IMAGE)
			.columns(Tables.IMAGE.ID, Tables.IMAGE.IMAGE_RESOLVABLE_ID, Tables.IMAGE.REPOSITORY_ID, Tables.IMAGE.REFERENCE_ID)
			.values(imageInRepoId, imageResolvableId, repoId, referenceId.toString())
			.execute();

		return imageInRepoId;
	}

	@Transactional
	public ImageResponse addEmptySyntheticImageWrap(UUID repoId) {
		UUID id = addEmptySyntheticImage(repoId);
		return imageRepository.loadImage(id).get();
	}

	private String subtr(String fileDir, String directory) {
		return fileDir.substring(directory.length());
	}

	private static Root findRoot(List<Root> roots, String fileDir) {
		for (Root root : roots) {
			if (fileDir.startsWith(root.directory())) {
				return root;
			}
		}
		return null;
	}
}
