package place.sita.labelle.gui.local.automation;

import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/automation.fxml", order = 5, tabName = "Automation")
public class AutomationTab {


	@FXML
	private SwingNode codeEditor;

	@FXML
	private TextField codeTextField;

	@FXML
	private TextField nameTextField;

	@FXML
	private ChoiceBox<PullUpdatesMode> pullUpdatesCheckBox;

	@FXML
	private ChoiceBox<RecursivelyMode> recursivelyCheckBox;

	@FXML
	private ChoiceBox<RunScriptsMode> runScriptsCheckBox;

	@PostFxConstruct
	public void setupEditor() {


		RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setCodeFoldingEnabled(true);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		codeEditor.setContent(sp);
	}

	@PostFxConstruct
	public void setupSampleData() {
		pullUpdatesCheckBox.setItems(FXCollections.observableArrayList(
			PullUpdatesMode.values()
		));
		pullUpdatesCheckBox.getSelectionModel().select(0);
		recursivelyCheckBox.setItems(FXCollections.observableArrayList(
			RecursivelyMode.values()
		));
		recursivelyCheckBox.getSelectionModel().select(0);
		runScriptsCheckBox.setItems(FXCollections.observableArrayList(
			RunScriptsMode.values()
		));
		runScriptsCheckBox.getSelectionModel().select(0);
	}

	private enum RecursivelyMode {
		ALL_LAYERS,
		EXCLUDING_LOWEST_LAYERS,
		;
	}

	private enum PullUpdatesMode {
		REFETCH_FROM_PARENT_IF_NEWER,
		;
	}

	private enum RunScriptsMode {
		RUN_FOR_UPDATED,
		RUN_FOR_ALL
		;
	}

}
