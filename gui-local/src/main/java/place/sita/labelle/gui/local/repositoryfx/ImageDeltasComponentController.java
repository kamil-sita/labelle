package place.sita.labelle.gui.local.repositoryfx;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.modulefx.annotations.FxDictatesHeight;
import place.sita.modulefx.annotations.FxNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/repository_deltas_image.fxml")
@FxDictatesHeight
public class ImageDeltasComponentController {

	public ImageDeltasComponentController() {
	}

	@FXML
	private CheckBox enableImageDeltaCheckBox;

	@FXML
	private TextField parentLocTextField;

	@FXML
	private TextField parentRootTextField;

	@FXML
	private TextField thisLocTextField;

	@FXML
	private TextField thisRootTextField;

	@FXML
	void applyImageDeltaPress(ActionEvent event) {

	}

	@FXML
	void calculateImageDeltaPress(ActionEvent event) {

	}

	@FXML
	void saveThisRootLocPress(ActionEvent event) {

	}
}
