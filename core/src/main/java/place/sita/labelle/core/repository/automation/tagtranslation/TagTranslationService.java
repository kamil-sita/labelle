package place.sita.labelle.core.repository.automation.tagtranslation;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.jooq.Tables;

import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.UUID;

@Service
public class TagTranslationService {

	private final DSLContext dslContext;

	public TagTranslationService(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Transactional(readOnly = true)
	public Optional<TagTranslation> getTagTranslation(UUID repositoryId) {
		return dslContext
				.select(Tables.TAG_TRANSLATION.TAG_LEVEL_TRANSLATION, Tables.TAG_TRANSLATION.TAGS_LEVEL_TRANSLATION, Tables.TAG_TRANSLATION.VALIDATION)
				.from(Tables.TAG_TRANSLATION)
				.where(Tables.TAG_TRANSLATION.REPOSITORY_ID.eq(repositoryId))
				.fetchOptional()
				.map(record -> new TagTranslation(record.get(Tables.TAG_TRANSLATION.TAG_LEVEL_TRANSLATION), record.get(Tables.TAG_TRANSLATION.TAGS_LEVEL_TRANSLATION), record.get(Tables.TAG_TRANSLATION.VALIDATION)));
	}

}
