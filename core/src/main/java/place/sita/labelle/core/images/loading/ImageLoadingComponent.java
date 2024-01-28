package place.sita.labelle.core.images.loading;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.core.utils.DelegatingFuture;
import place.sita.labelle.core.utils.Result2;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ImageLoadingComponent {

	private final Set<ImagePtr> loading = new HashSet<>();
	private final Map<ImagePtr, Future<Result2<BufferedImage, Exception>>> actualLoadingTasks = new HashMap<>();
	private final Map<ImagePtr, AtomicInteger> waiting = new HashMap<>();
	private final ExecutorService executors = Executors.newFixedThreadPool(2);

	private final List<ImageLoader> imageLoaders;

	public ImageLoadingComponent(List<ImageLoader> imageLoaders) {
		this.imageLoaders = imageLoaders;
	}

	public Future<Result2<BufferedImage, Exception>> load(ImagePtr imagePtr) {
		synchronized (this) {
			if (loading.contains(imagePtr)) {
				waiting.get(imagePtr).incrementAndGet();
				return createWrapperOverActualTask(imagePtr, actualLoadingTasks.get(imagePtr));
			} else {
				loading.add(imagePtr);
				waiting.put(imagePtr, new AtomicInteger(1));
				Future<Result2<BufferedImage, Exception>> actualLoadingTask = createLoadingTask(imagePtr);
				actualLoadingTasks.put(imagePtr, actualLoadingTask);
				return createWrapperOverActualTask(imagePtr, actualLoadingTask);
			}
		}
	}

	private Future<Result2<BufferedImage, Exception>> createLoadingTask(ImagePtr imagePtr) {
		return executors.submit(() -> {
			if (Thread.interrupted()) {
				return Result2.failure(new RuntimeException("Cancelled"));
			}
			try {
				return Result2.success(loadActual(imagePtr));
			} catch (Exception e) {
				return Result2.failure(e);
			}
		});
	}

	private BufferedImage loadActual(ImagePtr imagePtr) {
		for (var imageLoader : imageLoaders) {
			if (imageLoader.isSupported(imagePtr)) {
				return imageLoader.load(imagePtr);
			}
		}
		throw new IllegalStateException("No image loader supports this type of ImagePtr");
	}

	private Future<Result2<BufferedImage, Exception>> createWrapperOverActualTask(ImagePtr imagePtr, Future<Result2<BufferedImage, Exception>> actualLoadingTask) {
		return new DelegatingFuture<>(actualLoadingTask) {
			private boolean isCancelled = false;

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				synchronized (this) {
					isCancelled = true;
					int v = waiting.get(imagePtr).decrementAndGet();
					if (v == 0) {
						boolean cancelled = actualLoadingTask.cancel(mayInterruptIfRunning);
						waiting.remove(imagePtr);
						actualLoadingTasks.remove(imagePtr);
						return cancelled;
					}
					return false;
				}
			}

			@Override
			public boolean isCancelled() {
				return isCancelled;
			}
		};
	}

}
