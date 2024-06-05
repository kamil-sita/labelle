package place.sita.labelle.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SharedFutures<KeyT, ValueT> {
	private final Map<KeyT, AtomicInteger> waiting = new HashMap<>();
	private final Map<KeyT, Future<Result2<ValueT, Exception>>> actualLoadingTasks = new HashMap<>();
	private final Function<KeyT, Future<Result2<ValueT, Exception>>> loadingFunction;

	public SharedFutures(Function<KeyT, Future<Result2<ValueT, Exception>>> loadingFunction) {
		this.loadingFunction = loadingFunction;
	}


	public Future<Result2<ValueT, Exception>> load(KeyT key) {
		synchronized (this) {
			if (waiting.containsKey(key)) {
				waiting.get(key).incrementAndGet();
				return createWrapperOverActualTask(key, actualLoadingTasks.get(key));
			} else {
				waiting.put(key, new AtomicInteger(1));
				Future<Result2<ValueT, Exception>> actualLoadingTask = loadingFunction.apply(key);
				actualLoadingTasks.put(key, actualLoadingTask);
				return createWrapperOverActualTask(key, actualLoadingTask);
			}
		}
	}

	private Future<Result2<ValueT, Exception>> createWrapperOverActualTask(KeyT key, Future<Result2<ValueT, Exception>> actualLoadingTask) {
		return new DelegatingFuture<>(actualLoadingTask) {
			private boolean isCancelled = false;

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				synchronized (this) {
					isCancelled = true;
					var waitingC = waiting.get(key);
					int v = waitingC.decrementAndGet();
					if (v == 0) {
						boolean cancelled = actualLoadingTask.cancel(mayInterruptIfRunning);
						waiting.remove(key);
						actualLoadingTasks.remove(key);
						return cancelled;
					}
					return false; // technically, we couldn't cancel it?
				}
			}

			@Override
			public boolean isCancelled() {
				return isCancelled;
			}
		};
	}
}
