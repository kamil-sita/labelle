package place.sita.labelle.gui.local.root;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.Alerts;
import place.sita.labelle.core.images.imagelocator.ImageLocatorService;
import place.sita.labelle.core.images.imagelocator.Root;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.gui.local.fx.functional.FxFunctionalUi.ifSelected;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/root.fxml", order = 2, tabName = "Data roots")
public class RootFxTab implements MainMenuTab {

    private final ImageLocatorService imageLocatorService;

    @FXML
    private ListView<Root> rootListView;

    @FXML
    private TextField textField;

    public RootFxTab(ImageLocatorService imageLocatorService) {
        this.imageLocatorService = imageLocatorService;
    }

    @FXML
    public void addButtonPress(ActionEvent event) {
        imageLocatorService.createRoot(textField.getText())
            .onSuccess(root -> {
                rootObservableList.add(root);
                rootListView.getSelectionModel().select(root);
            })
            .onFailure(violation -> {
                Alerts.error("Roots cannot contain any other roots");
            });
    }

    @FXML
    void removeButtonPress(ActionEvent event) {
        ifSelected(rootListView)
            .then(root -> {
                imageLocatorService.removeRoot(root.id())
                    .onSuccess(nill -> {
                        rootListView.getItems().remove(root);
                    })
                    .onFailure(constraints -> {
                       Alerts.error("There are still images bound to this root. Removal not possible.");
                    });
            })
            .otherwise(nill -> {
                Alerts.error("Select a root first");
            });
    }

    @FXML
    void updateButtonPress(ActionEvent event) {

    }

    private ObservableList<Root> rootObservableList;

    @PostFxConstruct
    public void fetchData() {
        List<Root> roots = imageLocatorService.roots();
        rootObservableList = FXCollections.observableArrayList(roots);
        rootListView.setItems(rootObservableList);
    }

    @PostFxConstruct
    public void updateRootNameInTextField() {
        rootListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> textField.setText(newValue.directory()));
    }
}
