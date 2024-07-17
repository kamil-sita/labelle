package place.sita.labelle.gui.local.tagtranslation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.automation.tagtranslation.TagTranslationService;
import place.sita.labelle.core.repository.automation.tagtranslation.TagTranslationService.TagTranslationResult;
import place.sita.labelle.core.repository.automation.tagtranslation.TagTranslationService.TagTranslationResult.Failure;
import place.sita.labelle.core.repository.automation.tagtranslation.TagTranslationService.TagTranslationResult.Success;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.UnstableSceneEvent;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.ModuleFx;
import place.sita.modulefx.annotations.PostFxConstruct;
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


	@FXML
	private ChoiceBox<Repository> repositoryChoiceBox;

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

	@FXML
	private Button saveButton;

	@FXML
	private Label validationFailedLabel;

	@FXML
	private Label unsavedChangesLabel;

	private final KeyStone keyStone = keyStone();
	private final RepositoryService repositoryService;

	private final TagTranslationService tagTranslationService;

	public TagTranslationTab(TagTranslationService tagTranslationService, RepositoryService repositoryService) {
		this.tagTranslationService = tagTranslationService;
		this.repositoryService = repositoryService;
	}

	@PostFxConstruct
	public void setupRepositories() {
		ObservableList<Repository> repositories = FXCollections.observableArrayList();
		repositories.addAll(repositoryService.getRepositories());
		Platform.runLater(() -> {
			repositoryChoiceBox.setItems(repositories);
		});
	}

	private Repository selectedRepository;

	@PostFxConstruct
	public void onChangeOfRepository() {
		repositoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			selectedRepository = newValue;
			saveButton.setDisable(newValue == null);
			fetchRules(newValue);
		});
	}

	private void fetchRules(Repository newValue) {
		String tagLevel = "";
		String containerLevel = "";
		if (newValue != null) {
			var optionalTagTranslation = tagTranslationService.getTagTranslation(newValue.id());
			if (optionalTagTranslation.isPresent()) {
				tagLevel = optionalTagTranslation.get().tagLevel();
				containerLevel = optionalTagTranslation.get().tagsLevel();
			}
		}
		tagLevelRulesTextArea.setText(tagLevel);
		containerLevelRulesTextArea.setText(containerLevel);
		unsavedChangesLabel.setText("");
		doValidateActual();
	}

	@FXML
	public void onSavePress(ActionEvent event) {
		if (selectedRepository != null) {
			tagTranslationService.saveTagTranslation(selectedRepository.id(), tagLevelRulesTextArea.getText(), containerLevelRulesTextArea.getText());
			unsavedChangesLabel.setText("");
		}
	}

	@FXML
	public void onKeyTyped(KeyEvent event) {
		unsavedChangesLabel.setText("Changing repo will lose unsaved changes");
		doValidateActual();
	}

	private void doValidateActual() {
		UUID id = UUID.randomUUID();
		messageSender.send(new UnstableSceneEvent.MarkSceneAsUnstable(id, "Validating tag transformation"));
		validationFailedLabel.setText(null);
		Set<Tag> tags;
		try {
			tags = getTags();
		} catch (ArrayIndexOutOfBoundsException e) {
			// let's quietly ignore that - probably tags parsing error
			markStable(id);
			return;
		}
		String tagLevel = tagLevelRulesTextArea.getText();
		String containerLevel = containerLevelRulesTextArea.getText();
		Threading.onSeparateThread(keyStone, () -> markStable(id), toolkit -> {
			TagTranslationResult result = tagTranslationService.performTagTranslation(tagLevel, containerLevel, tags);
			switch (result) {
				case Failure failure -> {
					toolkit.onFxThread(() -> {
						validationResultsTextArea.setText("Validation failed:\r\n" + failure.message());
						toolkit.onSeparateThread(() -> {
							markStable(id);
						});
						validationFailedLabel.setText("Validation failed - those rules cannot be executed.");
					});
				}
				case Success success -> {
					toolkit.onFxThread(() -> {
						validationResultsTextArea.setText("Validation OK");
						setResults(success.tags());
						toolkit.onSeparateThread(() -> {
							markStable(id);
						});
					});
				}
			}
		});
	}

	private void markStable(UUID id) {
		messageSender.send(new UnstableSceneEvent.MarkSceneAsStable(id));
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
