package place.sita.labelle.extensions.tagging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.tasks.TaskContext;
import place.sita.labelle.core.tasks.TaskResult;
import place.sita.labelle.core.tasks.TaskType;
import place.sita.labelle.core.utils.Result2;
import place.sita.labelle.core.images.loading.ImageCachingLoader;
import place.sita.labelle.core.repository.taskapi.RepositoryApi;
import place.sita.labelle.core.tasks.scheduler.resources.resource.Resource;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class TagByRelyingOnWebuiSingleImage implements TaskType<TagByRelyingOnWebuiSingleImage.WebuiSingleImageArgs, RepositoryApi, TagByRelyingOnWebuiSingleImage.TaggingResult> {

	private final ImageCachingLoader imageCachingLoader;

	public TagByRelyingOnWebuiSingleImage(ImageCachingLoader imageCachingLoader) {
		this.imageCachingLoader = imageCachingLoader;
	}

	@Override
	public String code() {
		return "tag-webui-single-image";
	}

	@Override
	public String name() {
		return "Tag using webui (single image)";
	}

	@Override
	public TaskResult<TaggingResult> runTask(WebuiSingleImageArgs parameter, TaskContext<RepositoryApi> taskContext) { // todo shouldn't it just accept all exceptions?
		var futureImage = imageCachingLoader.load(taskContext.getApi().getInRepositoryService().getImagePtr(parameter.imageId));
		Result2<BufferedImage, Exception> results;
		try {
			results = futureImage.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException | TimeoutException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		if (results.isSuccess()) {
			BufferedImage image = results.getSuccess();
			String imageString = null;

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			try {
				ImageIO.write(image, "png", bos);
				byte[] imageBytes = bos.toByteArray();

				Base64.Encoder encoder = Base64.getEncoder();
				imageString = encoder.encodeToString(imageBytes);

				bos.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			String jsonPayload = String.format("{\"image\":\"%s\",\"model\":\"%s\"}", imageString, parameter.tagger);

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost("http://localhost:7861/sdapi/v1/interrogate");
			post.setHeader("Content-Type", "application/json");
			try {
				post.setEntity(new StringEntity(jsonPayload));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			taskContext.log("About to send query...");
			HttpResponse response = null;
			try {
				response = client.execute(post);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			HttpEntity entity = response.getEntity();
			taskContext.log("Received a response");

			String a = null;
			try {
				a = EntityUtils.toString(entity);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Response resp = null;
			try {
				resp = new ObjectMapper().readValue(a, Response.class);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}

			List<String> tags = applyTags(taskContext.getApi(), resp, parameter.imageId, parameter.tagger, taskContext);

			return TaskResult.success(new TaggingResult(parameter.tagger, parameter.imageId, tags));
		} else {
			throw (RuntimeException) results.getFailure();
		}
	}

	private List<String> applyTags(RepositoryApi repositoryApi, Response resp, UUID imageId, String tagger, TaskContext<?> ctx) {
		List<String> tags = new ArrayList<>();
		if (tagger.equals("clip")) {
			ctx.log("Got description: " + resp.caption);
			repositoryApi.getInRepositoryService().addTag(imageId, null, resp.caption, tagger);
			tags.add(resp.caption);
		} else {
			Arrays.stream(resp.caption.split(", ")).forEach(tag -> {
				ctx.log("Adding a tag: " + tag);
				repositoryApi.getInRepositoryService().addTag(imageId, null, tag, tagger);
				tags.add(tag);
			});
		}
		return tags;
	}

	public static class Response {
		private String caption;

		public String getCaption() {
			return caption;
		}

		public void setCaption(String caption) {
			this.caption = caption;
		}
	}

	@Override
	public String sampleValue() {
		return this.serializeParam(new WebuiSingleImageArgs("either of: clip/deepdanbooru", UUID.randomUUID()));
	}

	@Override
	public List<Resource<?>> resources(WebuiSingleImageArgs webuiSingleImageArgs) {
		return null; // todo actually CPU and HDD.
	}

	@Override
	public Class<RepositoryApi> contextType() {
		return RepositoryApi.class;
	}

	@Override
	public Class<WebuiSingleImageArgs> paramType() {
		return WebuiSingleImageArgs.class;
	}

	@Override
	public Class<TaggingResult> resultType() {
		return TaggingResult.class;
	}

	public record WebuiSingleImageArgs(String tagger, UUID imageId) {}

	public record TaggingResult(String tagger, UUID imageId, List<String> tags) {}
}
