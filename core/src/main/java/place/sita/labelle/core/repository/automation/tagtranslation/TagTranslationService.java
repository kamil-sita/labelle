package place.sita.labelle.core.repository.automation.tagtranslation;

import org.apache.logging.log4j.util.Strings;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.jooq.StringBuilderErrorListener;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory.InMemoryTagContainerInvokee;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.jooq.Tables;
import place.sita.tflang.TheFilteringLang;

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

	@Transactional
	public void saveTagTranslation(UUID repositoryId, String tagLevel, String tagsLevel) {
		String effectiveQuery = calculateEffectiveQuery(tagLevel, tagsLevel);
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		StringBuilder errors = new StringBuilder();
		Integer validationVersion = null;
		try {
			applyInstructions(invokee, effectiveQuery, errors);
			validationVersion = TheFilteringLang.VERSION;
		} catch (Exception e) {
			// ignore exceptions - we failed validation, but we're going to save the query anyway
		}

		dslContext
			.insertInto(Tables.TAG_TRANSLATION, Tables.TAG_TRANSLATION.REPOSITORY_ID, Tables.TAG_TRANSLATION.TAG_LEVEL_TRANSLATION, Tables.TAG_TRANSLATION.TAGS_LEVEL_TRANSLATION, Tables.TAG_TRANSLATION.VALIDATION)
			.values(repositoryId, tagLevel, tagsLevel, validationVersion)
			.onDuplicateKeyUpdate()
			.set(Tables.TAG_TRANSLATION.TAG_LEVEL_TRANSLATION, tagLevel)
			.set(Tables.TAG_TRANSLATION.TAGS_LEVEL_TRANSLATION, tagsLevel)
			.set(Tables.TAG_TRANSLATION.VALIDATION, validationVersion)
			.execute();
	}

	public TagTranslationResult performTagTranslation(String tagLevel, String tagsLevel, Set<Tag> tags) {
		String effectiveQuery = calculateEffectiveQuery(tagLevel, tagsLevel);
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		StringBuilder errors = new StringBuilder();
		try {
			applyInstructions(invokee, effectiveQuery, errors);
			SequencedSet<Tag> results = invokee.applyToInvokee(tags);
			if (!errors.isEmpty()) {
				return new TagTranslationResult.Failure(errors.toString());
			} else {
				return new TagTranslationResult.Success(results);
			}
		} catch (Exception e) {
			if (!errors.isEmpty()) {
				return new TagTranslationResult.Failure(errors.toString());
			} else {
				return new TagTranslationResult.Failure(e.getClass() + ", " + e.getMessage());
			}
		}
	}

	private static void applyInstructions(InMemoryTagContainerInvokee invokee, String effectiveQuery, StringBuilder errors) {
		invokee.applyInstructions(effectiveQuery, new StringBuilderErrorListener(errors));
	}

	private String calculateEffectiveQuery(String tagLevel, String tagsLevel) {
		if (Strings.isBlank(tagLevel)) {
			return tagsLevel;
		} else if (Strings.isBlank(tagsLevel)) {
			return tagLevel;
		} else {
			return tagLevel + ";\n" + tagsLevel;
		}
	}

	public sealed interface TagTranslationResult permits TagTranslationResult.Success, TagTranslationResult.Failure {

		record Success(SequencedSet<Tag> tags) implements TagTranslationResult {

		}

		record Failure(String message) implements TagTranslationResult {

		}

	}

}
