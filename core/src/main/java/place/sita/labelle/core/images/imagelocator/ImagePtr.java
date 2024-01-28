package place.sita.labelle.core.images.imagelocator;

public sealed interface ImagePtr {

	record ImageOnPath(String root, String path) implements ImagePtr {

	}

}
