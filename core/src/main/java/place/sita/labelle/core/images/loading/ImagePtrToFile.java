package place.sita.labelle.core.images.loading;

import place.sita.labelle.core.images.imagelocator.ImagePtr;

import java.io.File;
import java.util.Optional;

public class ImagePtrToFile {

	private ImagePtrToFile() {

	}

	public static Optional<File> toFile(ImagePtr imagePtr) {
		switch (imagePtr) {
			case ImagePtr.ImageOnPath imageOnPath -> {
				File file = new File(imageOnPath.root() + imageOnPath.path());
				return Optional.of(file);
			}
		}
	}

}
