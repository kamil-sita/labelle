package place.sita.labelle.gui.local.schedulerexecutionsfx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.ExecutionsService;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.annotations.FxNode;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/schedulerexecutions/scheduler_executions_view.fxml")
public class SchedulerExecutionViewFx {

	@FXML
	private TextArea resultsTextArea;

	@FXML
	private TextField taskResultTextField;

	@FXML
	private TextField creationDateTextField;

	@FXML
	private TextField parentTextField;

	@FXML
	private TextField typeTextField;

	@FXML
	private AnchorPane configuration;

	@FXML
	private TextField idTextField;

	@FxChild(patchNode = "configuration")
	private SchedulerExecutionViewConfigurationFx configurationFx;

	@FXML
	private AnchorPane history;

	@FxChild(patchNode = "history")
	private SchedulerExecutionViewHistoryFx historyFx;

	public void onJobSelected(ExecutionsService.ScheduledTaskResponse jobSelected) {
		if (jobSelected == null) {
			typeTextField.setText(null);
			creationDateTextField.setText(null);
			parentTextField.setText(null);
			historyFx.getJobExecutions(null);
			taskResultTextField.setText(null);
			configurationFx.onSelected(null);
			idTextField.setText(null);
		} else {
			typeTextField.setText(jobSelected.taskName());
			creationDateTextField.setText(jobSelected.jobCreationTime().toString());
			parentTextField.setText("not implemented yet");
			taskResultTextField.setText(jobSelected.taskStatus().toString());
			historyFx.getJobExecutions(jobSelected.taskId());
			configurationFx.onSelected(jobSelected);
			idTextField.setText(jobSelected.taskId().toString());
		}
	}
}
