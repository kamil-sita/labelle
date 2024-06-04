package place.sita.labelle.core.repository.inrepository;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.magicscheduler.TaskContext;
import place.sita.magicscheduler.TaskResult;
import place.sita.magicscheduler.TaskType;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.List;
import java.util.UUID;

@Component
public class AddImageTask implements TaskType<AddImageTask.AddImageTaskParameters, RepositoryApi, AddImageTask.AddImageResult> {

	public record AddImageTaskParameters(UUID repoId, String path) {
	}

	public record AddImageResult() {

	}

	@Override
	public String code() {
		return "add-image-v1";
	}

	@Override
	public String name() {
		return "Add image";
	}

	@Override
	public TaskResult<AddImageResult> runTask(AddImageTaskParameters parameter, TaskContext<RepositoryApi> taskContext) {
		var result = taskContext.getApi().getInRepositoryService().addImage(
			parameter.repoId(),
			parameter.path()
		);
		if (result.isSuccess()) {
			return TaskResult.success(new AddImageResult());
		} else {
			throw new RuntimeException("Failed to add image.");
		}
	}

	@Override
	public String sampleValue() {
		return serializeParam(new AddImageTaskParameters(UUID.randomUUID(), "C:/some_root/some_path.png"));
	}

	@Override
	public List<Resource<?>> resources(AddImageTaskParameters addImageTaskParameters) {
		return List.of();
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<AddImageTaskParameters> paramType() {
		return AddImageTaskParameters.class;
	}

	@Override
	public Class<AddImageResult> resultType() {
		return AddImageResult.class;
	}
}
