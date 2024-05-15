package place.sita.labelle.gui.local.schedulerexecutionsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.ExecutionsService;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.util.UUID;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/schedulerexecutions/scheduler_executions_view_history.fxml")
public class SchedulerExecutionViewHistoryFx {

	private final ExecutionsService executionsService;

	@FXML
	private TextArea configurationArea;

	@FXML
	private TextField executionResultTextField;

	@FXML
	private TextArea resultsArea;

	@FXML
	private ListView<ExecutionsService.ExecutionResponse> taskExecutionListView;

	@FXML
	private TextArea resultsTextArea;

	private ObservableList<ExecutionsService.ExecutionResponse> executionResponses;

	public SchedulerExecutionViewHistoryFx(ExecutionsService executionsService) {
		this.executionsService = executionsService;
	}

	@PostFxConstruct
	public void setupJobExecutions() {
		executionResponses = FXCollections.observableArrayList();
		taskExecutionListView.setItems(executionResponses);

		taskExecutionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				executionResultTextField.setText("");
				resultsArea.setText("");
				resultsArea.setDisable(true);
				configurationArea.setText(null);
			} else {
				resultsArea.setDisable(false);
				resultsArea.setText(newValue.log());
				executionResultTextField.setText(newValue.result().toString());
				configurationArea.setText(newValue.configuration());
			}
		});
	}

	public void getJobExecutions(UUID uuid) {
		if (uuid == null) {
			executionResponses.clear();
		} else {
			var execs = executionsService.getExecutions(uuid);
			executionResponses.clear();
			executionResponses.addAll(execs);
			if (!execs.isEmpty()) {
				taskExecutionListView.getSelectionModel().select(0);
			}
		}
	}
}
