package place.sita.labelle.core.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class DelegatingFuture<T> implements Future<T> {

	private final Future<T> actual;

	public DelegatingFuture(Future<T> actual) {
		this.actual = actual;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return actual.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return actual.isCancelled();
	}

	@Override
	public boolean isDone() {
		return actual.isDone();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return actual.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return actual.get(timeout, unit);
	}
}
