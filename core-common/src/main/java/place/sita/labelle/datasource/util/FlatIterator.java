package place.sita.labelle.datasource.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatIterator<T> implements CloseableIterator<T> {

	private final Iterator<Iterator<T>> metaIterator;
	private Iterator<T> currentIterator;
	private ExceptionlessAutoCloseable closeable;
	private boolean closed = false;

	public FlatIterator(Iterator<Iterator<T>> metaIterator) {
		this.metaIterator = metaIterator;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		closeable.close();
	}

	@Override
	public boolean hasNext() {
		if (closed) {
			throw new UseAfterCloseException();
		}
		if (currentIterator != null) {
			if (currentIterator.hasNext()) {
				return true;
			}
			currentIterator = null;
		}
		while (true) {
			if (!metaIterator.hasNext()) {
				return false;
			}
			currentIterator = metaIterator.next();
			if (currentIterator.hasNext()) {
				return true;
			}
		}
	}

	@Override
	public T next() {
		if (closed) {
			throw new UseAfterCloseException();
		}
		if (hasNext()) {
			return currentIterator.next();
		}
		throw new NoSuchElementException();
	}
}
