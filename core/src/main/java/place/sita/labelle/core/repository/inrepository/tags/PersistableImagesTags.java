package place.sita.labelle.core.repository.inrepository.tags;

import java.util.*;

/**
 * A structure to use if you want to quickly add a lot of tags to images and pass this info over to {@link TagRepository}.
 */
public class PersistableImagesTags {

	private final Map<UUID, Set<Tag>> tags = new LinkedHashMap<>();

	public void addTag(UUID imageId, String category, String tag) {
		addTag(imageId, new Tag(category, tag));
	}

	public void addTag(UUID imageId, Tag tag) {
		tags.computeIfAbsent(imageId, k -> new LinkedHashSet<>()).add(tag);
	}

	public Set<UUID> images() {
		return tags.keySet();
	}

	public Set<Tag> tags(UUID image) {
		return tags.getOrDefault(image, Collections.emptySet());
	}

}
