package place.sita.labelle.gui.local.repositoryfx;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RepositoryRuntimeStatistics {

	private final AtomicInteger imageLoadCount = new AtomicInteger(0);

	@EventListener
	public void onImageLoaded(ImageLoadedEvent event) {
		imageLoadCount.incrementAndGet();
	}

	public int getImageLoadCount() {
		return imageLoadCount.get();
	}

}
