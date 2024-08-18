package place.sita.labelle.extensions.tag.suggestions;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.persistence.JqRepo;
import place.sita.labelle.jooq.Tables;

import java.util.List;
import java.util.UUID;

@Component
public class TagSuggestions {
    private final DSLContext dsl;

    public TagSuggestions(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Transactional(readOnly = true)
    public List<String> getTagSuggestionsForImage(String category, String tag, UUID imageId) {
        UUID repoId = getRepoId(imageId);

        return
                dsl.select(Tables.IMAGE_TAGS.TAG)
                        .from(Tables.IMAGE_TAGS)
                        .where(Tables.IMAGE_TAGS.REPOSITORY_ID.eq(repoId))
                        .and(Tables.IMAGE_TAGS.TAG.likeIgnoreCase("%"+tag+"%"))
                        .and(Tables.IMAGE_TAGS.TAG_CATEGORY.likeIgnoreCase("%"+category+"%"))
                        .and(Tables.IMAGE_TAGS.IMAGE_ID.notEqual(imageId))
                        .limit(5)
                        .fetch(Tables.IMAGE_TAGS.TAG);
    }

    @Transactional(readOnly = true)
    public List<String> getCategorySuggestionsForImage(String category, String tag, UUID imageId) {
        UUID repoId = getRepoId(imageId);

        return
                dsl.select(Tables.IMAGE_TAGS.TAG_CATEGORY)
                        .from(Tables.IMAGE_TAGS)
                        .where(Tables.IMAGE_TAGS.REPOSITORY_ID.eq(repoId))
                        .and(Tables.IMAGE_TAGS.TAG.likeIgnoreCase("%"+tag+"%"))
                        .and(Tables.IMAGE_TAGS.TAG_CATEGORY.likeIgnoreCase("%"+category+"%"))
                        .and(Tables.IMAGE_TAGS.IMAGE_ID.notEqual(imageId))
                        .limit(5)
                        .fetch(Tables.IMAGE_TAGS.TAG_CATEGORY);
    }

    private UUID getRepoId(UUID imageId) {
        return JqRepo.fetchOne(() -> dsl
            .select(Tables.IMAGE.REPOSITORY_ID)
            .from(Tables.IMAGE)
            .where(Tables.IMAGE.ID.eq(imageId))
            .fetch());
    }
}
