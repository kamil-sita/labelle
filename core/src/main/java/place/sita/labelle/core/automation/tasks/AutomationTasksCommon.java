package place.sita.labelle.core.automation.tasks;

import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;

import java.util.UUID;

public class AutomationTasksCommon {

	private AutomationTasksCommon() {

	}

	public static void createRepo(String name, TaskContext<RepositoryApi> taskContext, UUID newRepoId) {
		taskContext
			.getApi()
			.getRepositoryService()
			.addRepository(newRepoId, name);
	}

}
