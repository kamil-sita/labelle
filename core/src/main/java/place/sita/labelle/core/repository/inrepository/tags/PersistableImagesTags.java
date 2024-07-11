package place.sita.labelle.core.repository.inrepository.tags;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A structure to use if you want to quickly add a lot of tags to images and pass this info over to {@link TagRepository}.
 */
public class PersistableImagesTags {

	private final UUID repositoryId;

	private final Map<UUID, Set<Tag>> tags = new LinkedHashMap<>();

	public PersistableImagesTags(UUID repositoryId) {
		this.repositoryId = repositoryId;
	}

	public PersistableImagesTags() {
		this.repositoryId = null;
	}

	public void addTag(UUID imageId, String tag, String category) {
		tags.computeIfAbsent(imageId, k -> new LinkedHashSet<>()).add(new Tag(tag, category));
	}

	public UUID repoId() {
		return repositoryId;
	}

	public Set<UUID> images() {
		return tags.keySet();
	}

	public Set<String> categories() {
		Set<String> families = new LinkedHashSet<>();
		for (var entry : tags.entrySet()) {
			for (var tagValue : entry.getValue()) {
				families.add(tagValue.category());
			}
		}
		return families;
	}

	public Set<Tag> tags() {
		Set<Tag> tagViews = new LinkedHashSet<>();
		for (var entry : tags.entrySet()) {
			for (var tagValue : entry.getValue()) {
				tagViews.add(new Tag(tagValue.tag(), tagValue.category()));
			}
		}
		return tagViews;
	}

	public Set<ImageTag> imageTags() {
		return tags.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream().map(tagValue -> new ImageTag(entry.getKey(), tagValue.tag(), tagValue.category())))
			.collect(Collectors.toSet());
	}

	public record ImageTag(UUID imageId, String tag, String category) {

	}

}
