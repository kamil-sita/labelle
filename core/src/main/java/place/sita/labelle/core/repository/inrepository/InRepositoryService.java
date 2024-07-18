package place.sita.labelle.core.repository.inrepository;

import org.apache.commons.lang3.NotImplementedException;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.core.repository.RootRepository;
import place.sita.labelle.core.repository.inrepository.delta.DeltaRepository;
import place.sita.labelle.core.repository.inrepository.delta.TagDeltaResponse;
import place.sita.labelle.core.repository.inrepository.image.ImageRepository;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.TagRepository;
import place.sita.labelle.core.utils.Result3;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;
import place.sita.labelle.datasource.cross.PreprocessableIdDataSourceWithRemoval;
import place.sita.labelle.jooq.Tables;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

import static place.sita.labelle.jooq.Tables.*;
import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Component
public class InRepositoryService {

    private final DSLContext dslContext;
    private final RootRepository rootRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final DeltaRepository deltaRepository;

    public InRepositoryService(
        DSLContext dslContext,
        RootRepository rootRepository,
        TagRepository tagRepository,
        ImageRepository imageRepository, DeltaRepository deltaRepository) {
        this.dslContext = dslContext;
        this.rootRepository = rootRepository;
	    this.tagRepository = tagRepository;
	    this.imageRepository = imageRepository;
        this.deltaRepository = deltaRepository;
    }


    public int count(UUID repositoryUuid, String query) {
        return dslContext
            .fetchCount(IMAGE, IMAGE.REPOSITORY_ID.eq(repositoryUuid));
    }

    public <Self extends PreprocessableIdDataSourceWithRemoval<UUID, ImageResponse, ImageRepository.FilteringApi<Self>, Self>> Self images() {
        return imageRepository.images();
    }

    public <Self extends PreprocessableDataSourceWithRemoval<TagDeltaResponse, DeltaRepository.FilteringApi<Self>, Self>> Self  tagDeltas() {
        return deltaRepository.tagDeltas();
    }

    public List<ImageResponse> images(UUID repositoryUuid, int offset, int limit, String query) {
        return imageRepository.images(repositoryUuid, offset, limit, query);
    }

    @Transactional
    public List<Root> roots() {
        return rootRepository.getRoots();
    }

    @Transactional
    public Result3<ImageResponse, DoesNotMatchAnyRoot, InsertFailedUnexpected> addImage(UUID repoId, File file) {
        String fileDir = file.getAbsolutePath();
        return addImage(repoId, fileDir);
    }

    @Transactional
    public Result3<ImageResponse, DoesNotMatchAnyRoot, InsertFailedUnexpected> addImage(UUID repoId, String absolutePath) {
        List<Root> roots = rootRepository.getRoots();
        Root r = findRoot(roots, absolutePath);
        if (r == null) {
            return Result3.failure1(new DoesNotMatchAnyRoot());
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
     * Note that this should not be used for children-parent repositories, but only for clones of repositories. See {@link InRepositoryService#referImage(UUID, UUID, String)}
     */
    @Transactional
    public UUID copyImage(UUID newRepoId, UUID originalImageId) {
        return copyOrRefer(newRepoId, originalImageId, CopyOrRefer.COPY, null);
    }

    @Transactional
    public UUID referImage(UUID newRepoId, UUID originalImageId, String persistentId) {
        return copyOrRefer(newRepoId, originalImageId, CopyOrRefer.REFER, persistentId);
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

        PersistableImagesTags pit = new PersistableImagesTags(originalImage.value3());
        getTags(selectedImageId).forEach(tag -> pit.addTag(newId, tag));
        addTags(pit);

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

    public void addTags(PersistableImagesTags persistableImagesTags) {
        tagRepository.addTags(persistableImagesTags);
    }

    @Transactional
    public void deleteImage(UUID id) {
        dslContext.delete(Tables.TAG_IMAGE)
            .where(Tables.TAG_IMAGE.IMAGE_ID.eq(id))
            .execute();

        dslContext.deleteFrom(IMAGE)
            .where(IMAGE.ID.eq(id))
            .execute();
    }

    public List<Tag> getTags(UUID imageId) {
        return tagRepository.getTags(imageId);
    }

    @Transactional
    public void addTag(UUID imageId, @Nullable UUID repositoryId, Tag tag) {
        tagRepository.addTag(imageId, repositoryId, tag);
    }

    @Transactional
    public void removeTag(UUID imageId, @Nullable UUID repositoryId, Tag tag) {
        tagRepository.deleteTag(imageId, repositoryId, tag);
    }

    @Transactional
    public void replaceTag(UUID imageId, @Nullable UUID repositoryId, String oldTag, String oldCategory, String newTag, String newCategory) {
        throw new NotImplementedException();
    }

    @Transactional
    public void addMarker(UUID imageId, String tag, String category, boolean shared) {
        throw new NotImplementedException();
    }

    @Transactional
    public void removeMarker(UUID imageId, String tag, String category) {
        throw new NotImplementedException();
    }

    @Transactional
    public void replaceMarker(UUID imageId, String oldTag, String oldCategory, String newTag, String newCategory, boolean shared) {
        throw new NotImplementedException();
    }

    @Transactional(readOnly = true)
    public List<TagDeltaResponse> getTagDeltas(UUID imageId) {
        return deltaRepository.getTagDeltas(imageId);
    }

    @Transactional(readOnly = true)
    public Optional<ImageResponse> getImageDelta(UUID imageId) {
        return deltaRepository.getImageDelta(imageId);
    }

    @Transactional(readOnly = true)
    public Set<Tag> parentTags(UUID imageId) {
        Set<UUID> parents = dslContext.select(PARENT_CHILD_IMAGE.PARENT_IMAGE_ID)
            .from(PARENT_CHILD_IMAGE)
            .where(PARENT_CHILD_IMAGE.CHILD_IMAGE_ID.eq(imageId))
            .fetchSet(PARENT_CHILD_IMAGE.PARENT_IMAGE_ID);

        List<Tag> tags = dslContext.select(IMAGE_TAGS.TAG, IMAGE_TAGS.TAG_CATEGORY)
            .from(IMAGE_TAGS)
            .where(IMAGE_TAGS.IMAGE_ID.in(parents))
            .fetch()
            .map(rr -> new Tag(rr.value1(), rr.value2()));

        return new LinkedHashSet<>(tags);
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

    public record DoesNotMatchAnyRoot() {

    }

    public record InsertFailedUnexpected() {

    }
}
