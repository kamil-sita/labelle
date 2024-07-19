package place.sita.labelle.gui.local.repositoryfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.Ids;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.gui.local.fx.Alerts;
import place.sita.modulefx.annotations.FxDictatesHeight;
import place.sita.modulefx.annotations.FxMessageListener;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.ModuleFx;
import place.sita.modulefx.messagebus.MessageSender;
import place.sita.modulefx.threading.Threading;

import java.util.UUID;

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

    @FXML
    private Button duplicateButton;

    @ModuleFx
    private MessageSender messageSender;

    @FXML
    private Button saveButton;

	public PersistentIdController(InRepositoryService inRepositoryService) {
		this.inRepositoryService = inRepositoryService;
	}

	@FXML
    public void onSavePress(ActionEvent event) {
        InRepositoryService.UpdateIdsResult result = inRepositoryService.updateIds(
            selectedImageId,
            persistentIdTextField.getText(),
            parentPersistentIdTextField.getText(),
            isVisibleForChildrenCheckBox.isSelected()
        );
        switch (result) {
	        case InRepositoryService.UpdateIdsResult.Success success -> { } // what we wanted, so let's ignore it
            case InRepositoryService.UpdateIdsResult.IdReuse idReuse -> {
                Alerts.error("Persistent ID is already in use");
            }
        }
    }

    @FXML
    public void onDuplicatePress(ActionEvent event) {
        UUID id = inRepositoryService.duplicateImage(selectedImageId);
        messageSender.send(new SelectImageEvent(id));
    }

    private final Threading.KeyStone keyStone = Threading.keyStone();
    private UUID selectedImageId;

    @FxMessageListener
    public void onImageSelected(ImageSelectedEvent event) {
        disable();
        selectedImageId = event.imageId();
        if (event.imageId() != null) {
            internalIdTextField.setText(event.imageId().toString());
            internalIdTextField.setDisable(false);
            duplicateButton.setDisable(false);
            saveButton.setDisable(false);

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
        } else {
            duplicateButton.setDisable(true);
        }
    }

    private void disable() {
        internalIdTextField.setText("");
        isVisibleForChildrenCheckBox.setSelected(false);
        parentPersistentIdTextField.setText("");
        persistentIdTextField.setText("");
        saveButton.setDisable(true);

        internalIdTextField.setDisable(true);
        isVisibleForChildrenCheckBox.setDisable(true);
        parentPersistentIdTextField.setDisable(true);
        persistentIdTextField.setDisable(true);
    }

}
