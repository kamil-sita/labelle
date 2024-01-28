package place.sita.labelle.core.images.loading;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.ImagePtr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class RootImageLoader implements ImageLoader {
	@Override
	public boolean isSupported(ImagePtr imagePtr) {
		return imagePtr instanceof ImagePtr.ImageOnPath;
	}

	@Override
	public BufferedImage load(ImagePtr imagePtr) {
		if (imagePtr instanceof ImagePtr.ImageOnPath imageOnPath) {
			try {
				File file = new File(imageOnPath.root() + imageOnPath.path());
				BufferedImage image = ImageIO.read(file);

				return image;
			} catch (IOException e) {
				throw new RuntimeException("Image could not be loaded", e);
			}
		} else {
			throw new IllegalStateException("Cannot be called like that"); //todo go over exceptions and don't use ISE
		}
	}
}
