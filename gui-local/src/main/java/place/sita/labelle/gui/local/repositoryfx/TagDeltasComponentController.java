package place.sita.labelle.gui.local.repositoryfx;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.modulefx.annotations.FxNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/repository_deltas_tags.fxml")
public class TagDeltasComponentController {

	public TagDeltasComponentController() {
	}

	@FXML
	private TableColumn<?, ?> deleteButtonTableColumn;

	@FXML
	private TextField deltaEntryTextField;

	@FXML
	private TextField deltaFamilyTextField;

	@FXML
	private TableView<?> deltaTagsTableView;

	@FXML
	private TableColumn<?, ?> deltaTypeTableColumn;

	@FXML
	private CheckBox enableTagDeltaCheckBox;

	@FXML
	private TableColumn<?, ?> familyDeltaTableColumn;

	@FXML
	private TableColumn<?, ?> familyEntryTableColumn;

	@FXML
	private TableColumn<?, ?> parentTagsEntryColumn;

	@FXML
	private TableView<?> parentTagsTableView;

	@FXML
	private TableColumn<?, ?> parentsTagsFamilyColumn;

	@FXML
	void addDeltaButtonPress(ActionEvent event) {

	}

	@FXML
	void applyTagsDeltaButtonPress(ActionEvent event) {

	}

	@FXML
	void calculateTagsDeltaButtonPress(ActionEvent event) {

	}

	@FXML
	void removeDeltaButtonPress(ActionEvent event) {

	}
}
