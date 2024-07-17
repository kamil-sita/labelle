package place.sita.labelle.gui.local.tagtranslation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory.InMemoryTagContainerInvokee;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.UnstableSceneEvent;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.ModuleFx;
import place.sita.modulefx.messagebus.MessageSender;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/tag_translation_rules.fxml", order = 15, tabName = "Tag translation")
public class TagTranslationTab implements MainMenuTab {
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
	public void doConversion(ActionEvent event) {
		UUID id = UUID.randomUUID();
		try {
			messageSender.send(new UnstableSceneEvent.MarkSceneAsUnstable(id, "Testing tag transformation"));

			Set<Tag> tags = new LinkedHashSet<>();

			Arrays.stream(testTagsBeforeTextArea.getText().split("\n")).forEach(s -> {
				if (!s.isBlank()) {
					String[] potentialTag = s.split(";");
					tags.add(new Tag(potentialTag[0], potentialTag[1]));
				}
			});

			String query1 = tagLevelRulesTextArea.getText();
			String query2 = containerLevelRulesTextArea.getText();
			InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
			invokee.applyInstructions(query1 + "\n;" + query2);
			Set<Tag> results = invokee.applyToInvokee(tags);

			StringBuilder sb = new StringBuilder();
			results.forEach(tag -> sb.append(tag.category()).append(";").append(tag.tag()).append("\n"));
			testTagsAfterTextArea.setText(sb.toString());
		} finally {
			messageSender.send(new UnstableSceneEvent.MarkSceneAsStable(id));
		}
	}

}
