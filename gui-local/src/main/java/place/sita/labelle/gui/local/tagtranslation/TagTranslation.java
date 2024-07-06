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
import place.sita.modulefx.annotations.FxTab;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/tag_translation_rules.fxml", order = 15, tabName = "Tag translation")
public class TagTranslation implements MainMenuTab {

	@FXML
	private ChoiceBox<?> repositoryChoiceBox;

	@FXML
	private TextArea tagLevelRulesTextArea;

	@FXML
	private TextArea testTagsAfterTextArea;

	@FXML
	private TextArea testTagsBeforeTextArea;

	@FXML
	void doConversion(ActionEvent event) {
		Set<Tag> tags = new LinkedHashSet<>();

		Arrays.stream(testTagsBeforeTextArea.getText().split("\n")).forEach(s -> {
			if (!s.isBlank()) {
				String[] potentialTag = s.split(";");
				tags.add(new Tag(potentialTag[0], potentialTag[1]));
			}
		});

		String query = tagLevelRulesTextArea.getText();
		InMemoryTagContainerInvokee invokee = new InMemoryTagContainerInvokee();
		invokee.applyInstructions(query);
		Set<Tag> results = invokee.applyToInvokee(tags);

		StringBuilder sb = new StringBuilder();
		results.forEach(tag -> sb.append(tag.category()).append(";").append(tag.tag()).append("\n"));
		testTagsAfterTextArea.setText(sb.toString());
	}

}
