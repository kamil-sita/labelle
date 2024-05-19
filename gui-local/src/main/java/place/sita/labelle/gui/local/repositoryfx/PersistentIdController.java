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
@FxNode(resourceFile = "/fx/repository/persistent_id_component.fxml")
@FxDictatesHeight
public class PersistentIdController {

    @FXML
    private TextField internalIdTextField;

    @FXML
    private CheckBox isVisibleForChildrenCheckBox;

    @FXML
    private TextField parentPersistentIdTextField;

    @FXML
    private TextField persistentIdTextField;

    @FXML
    void onSavePress(ActionEvent event) {

    }

}
