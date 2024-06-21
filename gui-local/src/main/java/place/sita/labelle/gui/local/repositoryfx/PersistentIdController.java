package place.sita.labelle.gui.local.repositoryfx;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.Ids;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.modulefx.annotations.FxDictatesHeight;
import place.sita.modulefx.annotations.FxMessageListener;
import place.sita.modulefx.annotations.FxNode;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import place.sita.modulefx.threading.Threading;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/persistent_id_component.fxml")
@FxDictatesHeight
public class PersistentIdController {

    private final InRepositoryService inRepositoryService;

    @FXML
    private TextField internalIdTextField;

    @FXML
    private CheckBox isVisibleForChildrenCheckBox;

    @FXML
    private TextField parentPersistentIdTextField;

    @FXML
    private TextField persistentIdTextField;

	public PersistentIdController(InRepositoryService inRepositoryService) {
		this.inRepositoryService = inRepositoryService;
	}


	@FXML
    void onSavePress(ActionEvent event) {

    }

    private final Threading.KeyStone keyStone = Threading.keyStone();

    @FxMessageListener
    public void onImageSelected(ImageSelectedEvent event) {
        disable();
        if (event.imageId() != null) {
            internalIdTextField.setText(event.imageId().toString());
            internalIdTextField.setDisable(false);

            Threading.onSeparateThread(keyStone, toolkit -> {
                Ids id = inRepositoryService.getIds(event.imageId());

                toolkit.onFxThread(() -> {
                    isVisibleForChildrenCheckBox.setSelected(id.visibleToChildren());
                    parentPersistentIdTextField.setText(id.parentPersistentId());
                    persistentIdTextField.setText(id.persistentId());

                    isVisibleForChildrenCheckBox.setDisable(false);
                    parentPersistentIdTextField.setDisable(false);
                    persistentIdTextField.setDisable(false);

                    parentPersistentIdTextField.setEditable(true);
                    persistentIdTextField.setEditable(true);
                });
            });
        }
    }

    private void disable() {
        internalIdTextField.setText("");
        isVisibleForChildrenCheckBox.setSelected(false);
        parentPersistentIdTextField.setText("");
        persistentIdTextField.setText("");

        internalIdTextField.setDisable(true);
        isVisibleForChildrenCheckBox.setDisable(true);
        parentPersistentIdTextField.setDisable(true);
        persistentIdTextField.setDisable(true);
    }

}
