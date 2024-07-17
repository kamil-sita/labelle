package place.sita.labelle.core.repository.automation.tagtranslation;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.logging.log4j.util.Strings;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory.InMemoryTagContainerInvokee;
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

	public TagTranslationResult performTagTranslation(String tagLevel, String tagsLevel, Set<Tag> tags) {
		String effectiveQuery = calculateEffectiveQuery(tagLevel, tagsLevel);
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		StringBuilder errors = new StringBuilder();
		try {
			invokee.applyInstructions(effectiveQuery, new BaseErrorListener() {
				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
					errors.append("line ").append(line).append(":").append(charPositionInLine).append(" ").append(msg).append("\r\n");
				}
			});
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
