package place.sita.labelle.gui.local.schedulerexecutionsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.LabPaginatorFactory;
import place.sita.labelle.gui.local.fx.LabPaginatorFactory.LabPaginator;
import place.sita.magicscheduler.ExecutionsService;
import place.sita.magicscheduler.TaskTypeRepository;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.util.UUID;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;


@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/schedulerexecutions/scheduler_executions.fxml", order = 1, tabName = "Scheduler Executions")
public class SchedulerExecutionTab {

    private final TaskTypeRepository taskTypeRepository;
    private final ExecutionsService executionsService;

    @FXML
    private Pagination taskPaginator;

    @FXML
    private ChoiceBox<TaskTypeRepository.TaskTypeResponse> taskTypeChoiceBox;

    @FXML
    private AnchorPane view;

    @FxChild(patchNode = "view")
    private SchedulerExecutionViewFx schedulerExecutionViewFx;

    public SchedulerExecutionTab(TaskTypeRepository taskTypeRepository, ExecutionsService executionsService) {
        this.taskTypeRepository = taskTypeRepository;
        this.executionsService = executionsService;
    }

    @PostFxConstruct
    public void setupTaskTypes() {
        ObservableList<TaskTypeRepository.TaskTypeResponse> observableList = FXCollections.observableArrayList();
        observableList.add(null);
        observableList.addAll(taskTypeRepository.allR());
        taskTypeChoiceBox.setItems(observableList);
    }

    private int pageSize = 100;

    @PostFxConstruct
    public void fetchOnTaskTypeKnown() {
        paginator = LabPaginatorFactory.factory(
            taskPaginator,
            pageSize,
            selected ->  executionsService.getJobsCountByTaskId(extractId(selected)),
            (paging, selected) -> executionsService.getScheduledTasks(paging.pageSize(), paging.offset(), extractId(selected)),
            selected -> onJobSelected(selected)
        );
        taskTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            paginator.hardReload(newValue);
        });
    }

    private LabPaginator<TaskTypeRepository.TaskTypeResponse> paginator;

    private void onJobSelected(ExecutionsService.ScheduledTaskResponse jobSelected) {
        schedulerExecutionViewFx.onJobSelected(jobSelected);
    }

    private UUID extractId(TaskTypeRepository.TaskTypeResponse newValue) {
        if (newValue == null) {
            return null;
        } else {
            return newValue.id();
        }
    }

}
