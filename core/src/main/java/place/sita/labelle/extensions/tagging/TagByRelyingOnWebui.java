package place.sita.labelle.extensions.tagging;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.tasks.TaskContext;
import place.sita.labelle.core.tasks.TaskResult;
import place.sita.labelle.core.tasks.TaskType;
import place.sita.labelle.core.repository.inrepository.ImageResponse;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.labelle.core.tasks.scheduler.RunPolicy;
import place.sita.labelle.core.tasks.scheduler.resources.resource.Resource;
import place.sita.labelle.extensions.tagging.TagByRelyingOnWebuiSingleImage.WebuiSingleImageArgs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TagByRelyingOnWebui implements TaskType<TagByRelyingOnWebui.WebuiArgs, RepositoryApi, TagByRelyingOnWebui.Results> {

	private final TagByRelyingOnWebuiSingleImage tagByRelyingOnWebuiSingleImage; // todo would it not be better to specify those by class?

	public TagByRelyingOnWebui(TagByRelyingOnWebuiSingleImage tagByRelyingOnWebuiSingleImage) {
		this.tagByRelyingOnWebuiSingleImage = tagByRelyingOnWebuiSingleImage;
	}

	@Override
	public String code() {
		return "tag-webui";
	}

	@Override
	public String name() {
		return "Tag using webui";
	}

	@Override
	public TaskResult<Results> runTask(WebuiArgs parameter, TaskContext<RepositoryApi> taskContext) {
		List<ImageResponse> images = taskContext.getApi().getInRepositoryService().images(parameter.repositoryId, 0, Integer.MAX_VALUE, "");
		List<UUID> scheduled = new ArrayList<>();

		for (var image : images) {
			WebuiSingleImageArgs singleImageArgs = new WebuiSingleImageArgs(parameter.tagger, image.id());
			UUID id = taskContext.submitAnotherTask(tagByRelyingOnWebuiSingleImage, singleImageArgs, RunPolicy.ifJobSucceeded());
			scheduled.add(id);
		}

		return TaskResult.success(new Results(parameter.tagger, parameter.repositoryId, scheduled));
	}

	@Override
	public String sampleValue() {
		return this.serializeParam(new WebuiArgs("either of: clip/deepdanbooru", UUID.randomUUID()));
	}

	@Override
	public List<Resource<?>> resources(WebuiArgs webuiArgs) {
		return null;
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<WebuiArgs> paramType() {
		return WebuiArgs.class;
	}

	@Override
	public Class<Results> resultType() {
		return Results.class;
	}

	public record WebuiArgs(String tagger, UUID repositoryId) {}

	public record Results(String tagger, UUID repositoryId, List<UUID> scheduled) {}
}
