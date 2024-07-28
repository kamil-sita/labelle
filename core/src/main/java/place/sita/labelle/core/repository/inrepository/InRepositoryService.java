package place.sita.labelle.core.repository.inrepository;

import org.apache.commons.lang3.NotImplementedException;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.repository.RootRepository;
import place.sita.labelle.core.repository.inrepository.delta.DeltaRepository;
import place.sita.labelle.core.repository.inrepository.delta.TagDeltaResponse;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.inrepository.image.ImageService;
import place.sita.labelle.core.repository.inrepository.tags.PersistableImagesTags;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.TagRepository;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;
import place.sita.labelle.jooq.Tables;

import javax.annotation.Nullable;
import java.util.*;

import static place.sita.labelle.jooq.Tables.IMAGE_TAGS;
import static place.sita.labelle.jooq.Tables.PARENT_CHILD_IMAGE;
import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Component
public class InRepositoryService {

    private final DSLContext dslContext;
    private final RootRepository rootRepository;
    private final TagRepository tagRepository;
    private final DeltaRepository deltaRepository;
    private final ImageService imageService;

    public InRepositoryService(
	    DSLContext dslContext,
	    RootRepository rootRepository,
	    TagRepository tagRepository,
        DeltaRepository deltaRepository,
        ImageService imageService) {
        this.dslContext = dslContext;
        this.rootRepository = rootRepository;
	    this.tagRepository = tagRepository;
        this.deltaRepository = deltaRepository;
	    this.imageService = imageService;
    }

    public ImageService images() {
        return imageService;
    }

    public <Self extends PreprocessableDataSourceWithRemoval<TagDeltaResponse, DeltaRepository.FilteringApi<Self>, Self>> Self  tagDeltas() {
        return deltaRepository.tagDeltas();
    }

    @Transactional
    public List<Root> roots() {
        return rootRepository.getRoots();
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
    public void addTag(UUID imageId, Tag tag) {
        tagRepository.addTag(imageId, tag);
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

        List<Tag> tags = dslContext.select(IMAGE_TAGS.TAG_CATEGORY, IMAGE_TAGS.TAG)
            .from(IMAGE_TAGS)
            .where(IMAGE_TAGS.IMAGE_ID.in(parents))
            .fetch()
            .map(rr -> new Tag(rr.value1(), rr.value2()));

        return new LinkedHashSet<>(tags);
    }

    public record DoesNotMatchAnyRoot() {

    }

    public record InsertFailedUnexpected() {

    }
}
