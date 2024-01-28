package place.sita.labelle.core.images.loading;

import place.sita.labelle.core.images.imagelocator.ImagePtr;

import java.awt.image.BufferedImage;

public interface ImageLoader {

	boolean isSupported(ImagePtr imagePtr);

	BufferedImage load(ImagePtr imagePtr);

}
