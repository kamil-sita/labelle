package place.sita.labelle.gui.local.tagtranslation;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory.InMemoryTagContainerInvokee;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.UnstableSceneEvent;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.ModuleFx;
import place.sita.modulefx.messagebus.MessageSender;
import place.sita.modulefx.threading.Threading;
import place.sita.modulefx.threading.Threading.KeyStone;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.modulefx.threading.Threading.keyStone;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/tag_translation_rules.fxml", order = 15, tabName = "Tag translation")
public class TagTranslationTab implements MainMenuTab {
	private static final Logger logger = LoggerFactory.getLogger(TagTranslationTab.class);
	@FXML
	private ChoiceBox<?> repositoryChoiceBox;

	@FXML
	private TextArea tagLevelRulesTextArea;

	@FXML
	private TextArea containerLevelRulesTextArea;

	@FXML
	private TextArea testTagsAfterTextArea;

	@FXML
	private TextArea testTagsBeforeTextArea;

	@ModuleFx
	private MessageSender messageSender;

	@FXML
	private TextArea validationResultsTextArea;

	private final KeyStone keyStone = keyStone();

	@FXML
	public void onKeyTyped(KeyEvent event) {
		Threading.onSeparateThread(keyStone, toolkit -> {
			UUID id = UUID.randomUUID();
			try  {
				messageSender.send(new UnstableSceneEvent.MarkSceneAsUnstable(id, "Validating tag transformation"));

				Set<Tag> tags = getTags();
				String query = getQuery();
				InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
				StringBuilder errors = new StringBuilder();
				try {
					invokee.applyInstructions(query, new BaseErrorListener() {
						@Override
						public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
							errors.append("line ").append(line).append(":").append(charPositionInLine).append(" ").append(msg).append("\r\n");
						}
					});

					if (errors.isEmpty()) {
						toolkit.onFxThread(() -> {
							validationResultsTextArea.setText("Validation OK");
							toolkit.onSeparateThread(() -> {
								Set<Tag> results = invokee.applyToInvokee(tags);
								toolkit.onFxThread(() -> {
									setResults(results);
								});
							});
						});
					} else {
						toolkit.onFxThread(() -> validationResultsTextArea.setText("Validation failed: \r\n" + errors));
					}

				} catch (Exception e) {
					if (errors.isEmpty()) {
						toolkit.onFxThread(() ->
							validationResultsTextArea.setText("Validation failed: \n" + e.getClass() + ", " + e.getMessage())
						);
						logger.error("Validation failed", e);
					} else {
						toolkit.onFxThread(() -> validationResultsTextArea.setText("Validation failed: \n" + errors));
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// let's quietly ignore that - probably tags parsing error
			} finally {
				messageSender.send(new UnstableSceneEvent.MarkSceneAsStable(id));
			}
		});
	}

	private String getQuery() {
		String query1 = tagLevelRulesTextArea.getText();
		String query2 = containerLevelRulesTextArea.getText();
		if (Strings.isBlank(query1)) {
			return query2;
		} else if (Strings.isBlank(query2)) {
			return query1;
		} else {
			return query1 + ";\n" + query2;
		}
	}

	private void setResults(Set<Tag> results) {
		StringBuilder sb = new StringBuilder();
		results.forEach(tag -> sb.append(tag.category()).append(";").append(tag.tag()).append("\n"));
		testTagsAfterTextArea.setText(sb.toString());
	}

	private Set<Tag> getTags() {
		Set<Tag> tags = new LinkedHashSet<>();

		Arrays.stream(testTagsBeforeTextArea.getText().split("\n")).forEach(s -> {
			if (!s.isBlank()) {
				String[] potentialTag = s.split(";");
				tags.add(new Tag(potentialTag[0], potentialTag[1]));
			}
		});
		return tags;
	}

}
