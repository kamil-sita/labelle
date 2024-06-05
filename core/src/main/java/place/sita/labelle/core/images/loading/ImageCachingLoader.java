package place.sita.labelle.core.images.loading;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.imagelocator.ImagePtr;
import place.sita.labelle.core.shutdown.Shutdownable;
import place.sita.labelle.core.utils.CompletedFuture;
import place.sita.labelle.core.utils.Result2;
import place.sita.labelle.core.utils.SharedFutures;
import place.sita.labelle.core.utils.cache.SoftHardCache;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ImageCachingLoader implements Shutdownable {

	private final Object synchronizationObject = new Object();


	private final ImageLoadingComponent imageLoadingComponent;
	private final SoftHardCache<ImagePtr, Result2<BufferedImage, Exception>> softHardCache = new SoftHardCache<>();
	private final SharedFutures<ImagePtr, BufferedImage> sharedFutures;
	private final ExecutorService executorService = Executors.newFixedThreadPool(2);


	public ImageCachingLoader(ImageLoadingComponent imageLoadingComponent) {
		this.imageLoadingComponent = imageLoadingComponent;
		sharedFutures = new SharedFutures<>(imagePtr -> {
			return loadAsFutureImpl(imagePtr);
		});
	}

	public void preload(ImagePtr imagePtr) {
		load(imagePtr);
	}

	public Future<Result2<BufferedImage, Exception>> load(ImagePtr imagePtr) {
		synchronized (synchronizationObject) {
			Optional<Result2<BufferedImage, Exception>> cachedResult = softHardCache.getFromCache(imagePtr);
			if (cachedResult.isPresent()) {
				return new CompletedFuture<>(cachedResult.get());
			}
			return sharedFutures.load(imagePtr);
		}
	}

	private final Set<ImagePtr> planedLoads = new HashSet<>();
	private final Map<ImagePtr, Semaphore> loadsWaits = new HashMap<>();
	private final Map<ImagePtr, AtomicInteger> loadsAwaiting = new HashMap<>();

	private Future<Result2<BufferedImage, Exception>> loadAsFutureImpl(ImagePtr imagePtr) {
		return executorService.submit(() -> {
			return loadImageActual(imagePtr);
		});
	}

	private Result2<BufferedImage, Exception> loadImageActual(ImagePtr imagePtr) {
		Semaphore semaphore = null;
		synchronized (synchronizationObject) {
			if (planedLoads.contains(imagePtr)) {
				loadsAwaiting.get(imagePtr).incrementAndGet();
				semaphore = loadsWaits.get(imagePtr);
			}
		}

		if (semaphore != null) {
			semaphore.acquireUninterruptibly();
			synchronized (synchronizationObject) {
				var optionalResult = softHardCache.getFromCache(imagePtr);
				if (optionalResult.isPresent()) {
					return optionalResult.get();
				}
			}
			// it is not present even though it was just loaded??
			return loadImageActual(imagePtr); // well, let's retry it? This should NEVER happen
		} else {
			synchronized (synchronizationObject) {
				planedLoads.add(imagePtr);
				loadsWaits.put(imagePtr, new Semaphore(0));
				loadsAwaiting.put(imagePtr, new AtomicInteger(0));
			}
			var futureResult = imageLoadingComponent.load(imagePtr);
			Result2<BufferedImage, Exception> result;
			try {
				result = futureResult.get();
			} catch (Exception e) {
				result = Result2.failure(e);
			}
			synchronized (synchronizationObject) {
				planedLoads.remove(imagePtr);
				softHardCache.putIntoCache(imagePtr, result);
				loadsWaits.get(imagePtr).release(loadsAwaiting.get(imagePtr).get());
				loadsWaits.remove(imagePtr);
				loadsAwaiting.remove(imagePtr);
			}
			return result;
		}
	}

	@Override
	public void shutdown() {
		executorService.shutdown();
	}
}
