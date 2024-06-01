package place.sita.labelle.core.repository.inrepository.image;

import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.datasource.Identifiable;

import java.util.UUID;

public record ImageResponse(UUID id, String root, String path) implements Identifiable<UUID> {

	@Override
	public String toString() {
		return root + path;
	}

	public ImagePtr toPtr() {
		return new ImagePtr.ImageOnPath(root, path);
	}

	@Override
	public UUID getId() {
		return id;
	}
}
