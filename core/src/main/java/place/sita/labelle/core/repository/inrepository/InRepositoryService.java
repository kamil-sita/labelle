package place.sita.labelle.core.repository.inrepository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.core.repository.RootRepository;
import place.sita.labelle.core.utils.Result3;
import place.sita.labelle.jooq.Tables;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.UUID;

import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Component
public class InRepositoryService {

    private final DSLContext dslContext;
    private final RootRepository rootRepository;
    private final TagRepository tagRepository;

    public InRepositoryService(DSLContext dslContext, RootRepository rootRepository, TagRepository tagRepository) {
        this.dslContext = dslContext;
        this.rootRepository = rootRepository;
	    this.tagRepository = tagRepository;
    }


    public int count(UUID repositoryUuid, String query) {
        return dslContext
            .fetchCount(IMAGE, IMAGE.REPOSITORY_ID.eq(repositoryUuid));
    }

    public List<ImageResponse> images(UUID repositoryUuid, int offset, int limit, String query) {
        return dslContext
            .select(IMAGE.ID, IMAGE.imageResolvable().imageFile().RELATIVE_DIR, IMAGE.imageResolvable().imageFile().root().ROOT_DIR)
            .from(IMAGE)
            .where(IMAGE.REPOSITORY_ID.equal(repositoryUuid))
            .orderBy(IMAGE.ID)
            .limit(limit)
            .offset(offset)
            .fetch()
            .map(rr -> {
                return new ImageResponse(rr.value1(), rr.value3(), rr.value2());
            });
    }

    @Transactional
    public List<Root> roots() {
        return rootRepository.getRoots();
    }

    @Transactional
    public Result3<ImageResponse, DoesNotMatchAnyRoot, InsertFailedUnexpected> addImage(UUID repoId, File file) {
        List<Root> roots = rootRepository.getRoots();
        String fileDir = file.getAbsolutePath();
        Root r = findRoot(roots, fileDir);
        if (r == null) {
            return Result3.failure1(new DoesNotMatchAnyRoot());
        }
        // todo transaction
        String relPath = subtr(fileDir, r.directory());
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
     * Note that this should not be used for children-parent repositories, but only for clones of repositories. See {@link InRepositoryService#referImage(UUID, UUID)}
     */
    @Transactional
    public UUID copyImage(UUID newRepoId, UUID originalImageId) {
        return copyOrRefer(newRepoId, originalImageId, CopyOrRefer.COPY);
    }

    @Transactional
    public UUID referImage(UUID newRepoId, UUID originalImageId) {
        return copyOrRefer(newRepoId, originalImageId, CopyOrRefer.REFER);
    }

    private enum CopyOrRefer {
        COPY,
        REFER;
    }

    private UUID copyOrRefer(UUID newRepoId, UUID originalImageId, CopyOrRefer copyOrRefer) {
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

        // todo reference logic

        String referenceId = JqRepo.fetchOne(() ->
            dslContext
                .select(IMAGE.REFERENCE_ID)
                .from(IMAGE)
                .where(IMAGE.ID.eq(originalImageId))
                .fetch()
        );

        // todo we can probably do with less queries. And better copying, not one by one.

        JqRepo.insertOne(() ->
            dslContext.insertInto(IMAGE)
                .columns(IMAGE.ID, IMAGE.IMAGE_RESOLVABLE_ID, IMAGE.REPOSITORY_ID, IMAGE.REFERENCE_ID)
                .values(newImageId, underlyingImageResolvable, newRepoId, referenceId)
                .execute()
        );

        return newImageId;
    }

    // todo current approach is likely very unoptimized. Why send a job only to re-retrieve the data later? Can it reasonably change?
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

    public record TagResponse(String tag, String family) {

    }

    public List<TagResponse> getTags(UUID imageId) {
        return tagRepository.getTags(imageId)
            .stream()
            .map(tv -> new TagResponse(tv.value(), tv.family()))
            .toList();
    }

    @Transactional
    public void addTag(UUID imageId, @Nullable UUID repositoryId, String tag, String family) {
        tagRepository.addTag(imageId, repositoryId, tag, family);
    }

    @Transactional
    public void removeTag(UUID imageId, @Nullable UUID repositoryId, String tag, String family) {
        tagRepository.deleteTag(imageId, repositoryId, tag, family);
    }

    @Transactional
    public void replaceTag(UUID imageId, @Nullable UUID repositoryId, String oldTag, String oldFamily, String newTag, String newFamily) {
        removeTag(imageId, repositoryId, oldTag, oldFamily);
        addTag(imageId, repositoryId, newTag, newFamily);
    }

    @Transactional
    public void addMarker(UUID imageId, String tag, String family, boolean shared) {

    }

    @Transactional
    public void removeMarker(UUID imageId, String tag, String family) {

    }

    @Transactional
    public void replaceMarker(UUID imageId, String oldTag, String oldFamily, String newTag, String newFamily, boolean shared) {
        removeMarker(imageId, oldTag, oldFamily);
        addMarker(imageId, newTag, newFamily, shared);
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
