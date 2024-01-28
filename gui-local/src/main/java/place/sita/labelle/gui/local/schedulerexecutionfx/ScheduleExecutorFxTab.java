package place.sita.labelle.gui.local.schedulerexecutionfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.tasks.TasksService;
import place.sita.labelle.core.tasks.transfer.KnownTaskTypesResponse;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.gui.local.fx.functional.FxFunctionalUi.ifSelected;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/scheduler_executor.fxml", order = 0, tabName = "Scheduler Executor FX")
public class ScheduleExecutorFxTab {
    private final TasksService tasksService;

    public ScheduleExecutorFxTab(TasksService tasksService) {
        this.tasksService = tasksService;
    }

    private FilteredList<KnownTaskTypesResponse> observableKnownTaskTypes;
    private  ObservableList<KnownTaskTypesResponse> observableListOfKnownTaskTypesResponse;

    @PostFxConstruct
    public void setTasks() {
        observableListOfKnownTaskTypesResponse = FXCollections.observableArrayList();
        observableListOfKnownTaskTypesResponse.addAll(tasksService.getTaskTypes());
        observableKnownTaskTypes = new FilteredList<>(observableListOfKnownTaskTypesResponse);
        schedulableTaskList.setItems(observableKnownTaskTypes);
    }

    @PostFxConstruct
    public void setUpFiltering() {
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            observableKnownTaskTypes.setPredicate(kttr -> {
                String stringRepresentation = kttr.toString();
                return stringRepresentation.contains(newValue);
            });
        });
    }

    @PostFxConstruct
    public void setupOnSelect() {
        schedulableTaskList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                codeLabel.setText(newValue.code());
                nameLabel.setText(newValue.name());
                codeArea.setText(newValue.sampleValue());
                validateButton.setDisable(false);
                executeButton.setDisable(false);
            } else {
                codeLabel.setText("???");
                nameLabel.setText("???");
                codeArea.setText(null);
                validateButton.setDisable(true);
                executeButton.setDisable(true);
            }
        });
    }

    @FXML
    private Button validateButton;
    @FXML
    private Button executeButton;

    @FXML
    private TextField filterTextField;

    @FXML
    private Label codeLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private ListView<KnownTaskTypesResponse> schedulableTaskList;

    @FXML
    private TextArea codeArea;

    @FXML
    void executeButtonPress(ActionEvent event) {
        ifSelected(schedulableTaskList)
            .then(kttr -> {
                tasksService.submitTask(kttr.code(), codeArea.getText());
            });
    }

    @FXML
    void validateButtonPress(ActionEvent event) {
    }
}
