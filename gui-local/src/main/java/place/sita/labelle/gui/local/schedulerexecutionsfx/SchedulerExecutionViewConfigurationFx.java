package place.sita.labelle.gui.local.schedulerexecutionsfx;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.Alerts;
import place.sita.labelle.gui.local.fx.threading.Threading;
import place.sita.labelle.core.tasks.ExecutionsService;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.PostFxConstruct;
import place.sita.labelle.jooq.enums.TaskStatus;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.gui.local.fx.functional.FxFunctionalUi.ifSelected;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/schedulerexecutions/scheduler_executions_view_configuration.fxml")
public class SchedulerExecutionViewConfigurationFx {

	private final ExecutionsService executionsService;

	@FXML
	private TextArea configurationTextArea;

	@FXML
	private ListView<ExecutionsService.DependencyResponse> dependenciesListView;

	@FXML
	private ListView<ExecutionsService.DependencyResponse> isDependencyListView;

	@FXML
	private Button removeDependencyButton;

	@FXML
	private ChoiceBox<TaskStatus> taskResultChoiceBox;

	@FXML
	private Button overrideButton;

	public SchedulerExecutionViewConfigurationFx(ExecutionsService executionsService) {
		this.executionsService = executionsService;
	}

	private UUID selectedTaskId;

	public void onSelected(ExecutionsService.ScheduledTaskResponse jobSelected) {
		if (jobSelected == null) {
			selectedTaskId = null;
			taskResultChoiceBox.getSelectionModel().clearSelection();
			taskResultChoiceBox.setDisable(true);
			removeDependencyButton.setDisable(true);
		} else {
			selectedTaskId = jobSelected.taskId();
			taskResultChoiceBox.getSelectionModel().select(jobSelected.taskStatus());
			taskResultChoiceBox.setDisable(false);
			removeDependencyButton.setDisable(false);
		}
		updateDependencies(jobSelected);
	}

	private final Threading.KeyStone keyStone = Threading.keyStone();

	private void updateDependencies(ExecutionsService.ScheduledTaskResponse jobSelected) {
		keyStone.cancel();
		if (jobSelected != null) {
			Threading.onSeparateThread(keyStone, toolkit -> {
				var response = executionsService.getTaskConfiguration(jobSelected.taskId());
				toolkit.onFxThread(() -> {
					dependenciesListView.setItems(FXCollections.observableArrayList(response.dependsOn()));
					isDependencyListView.setItems(FXCollections.observableArrayList(response.isDependencyFor()));
					taskResultChoiceBox.setDisable(false);
					taskResultChoiceBox.getSelectionModel().select(response.status());
					configurationTextArea.setText(response.configuration());
				});
			});
		} else {
			dependenciesListView.setItems(FXCollections.observableArrayList());
			isDependencyListView.setItems(FXCollections.observableArrayList());
			taskResultChoiceBox.setDisable(true);
			taskResultChoiceBox.getSelectionModel().clearSelection();
			configurationTextArea.setText(null);
		}
	}

	@PostFxConstruct
	public void setupTaskResults() {
		taskResultChoiceBox.setItems(FXCollections.observableArrayList(TaskStatus.values()));
	}

	@FXML
	void removeDependencyPress(ActionEvent event) {
		ifSelected(dependenciesListView).then(dependency -> {
			dependenciesListView.getItems().remove(dependency);
		});
	}

	@FXML
	void overrideButtonPress(ActionEvent event) {
		if (selectedTaskId != null) {
			Set<UUID> preservedDependencies = dependenciesListView.getItems().stream().map(ExecutionsService.DependencyResponse::taskId).collect(Collectors.toSet());
			Threading.onSeparateThread(toolkit -> {
				var results = executionsService.override(selectedTaskId, preservedDependencies, taskResultChoiceBox.getSelectionModel().getSelectedItem(), configurationTextArea.getText());
				toolkit.onFxThread(() -> {
					results.onFailure(v -> {
						Alerts.error("Failed to override task");
					});
				});
			});
		}
	}
}
