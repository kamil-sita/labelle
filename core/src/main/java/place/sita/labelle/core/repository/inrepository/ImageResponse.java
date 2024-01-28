package place.sita.labelle.core.repository.inrepository;

import place.sita.labelle.core.images.imagelocator.ImagePtr;

import java.util.UUID;

public record ImageResponse(UUID id, String root, String path) {

	@Override
	public String toString() {
		return root + path;
	}

	public ImagePtr toPtr() {
		return new ImagePtr.ImageOnPath(root, path);
	}
}
