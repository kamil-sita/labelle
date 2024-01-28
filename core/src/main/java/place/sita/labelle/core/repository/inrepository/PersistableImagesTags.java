package place.sita.labelle.core.repository.inrepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A structure to use if you want to quickly add a lot of tags to images and pass this info over to {@link TagRepository}.
 */
public class PersistableImagesTags {

	private final UUID repositoryId;

	private final Map<UUID, Set<TagValue>> tags = new LinkedHashMap<>();

	public PersistableImagesTags(UUID repositoryId) {
		this.repositoryId = repositoryId;
	}

	public PersistableImagesTags() {
		this.repositoryId = null;
	}

	public void addTag(UUID imageId, String tag, String family) {
		tags.computeIfAbsent(imageId, k -> new LinkedHashSet<>()).add(new TagValue(tag, family));
	}

	public UUID repoId() {
		return repositoryId;
	}

	public Set<UUID> images() {
		return tags.keySet();
	}

	public Set<String> families() {
		Set<String> families = new LinkedHashSet<>();
		for (var entry : tags.entrySet()) {
			for (var tagValue : entry.getValue()) {
				families.add(tagValue.family);
			}
		}
		return families;
	}

	public Set<TagRepository.TagView> tags() {
		Set<TagRepository.TagView> tagViews = new LinkedHashSet<>();
		for (var entry : tags.entrySet()) {
			for (var tagValue : entry.getValue()) {
				tagViews.add(new TagRepository.TagView(tagValue.tag, tagValue.family));
			}
		}
		return tagViews;
	}

	public Set<ImageTag> imageTags() {
		return tags.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream().map(tagValue -> new ImageTag(entry.getKey(), tagValue.tag, tagValue.family)))
			.collect(Collectors.toSet());
	}

	private record TagValue(String tag, String family) { // todo we could use a single representation

	}

	public record ImageTag(UUID imageId, String tag, String family) {

	}

}
