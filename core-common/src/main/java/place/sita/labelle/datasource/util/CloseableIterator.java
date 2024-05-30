package place.sita.labelle.datasource.util;

import java.util.Iterator;
import java.util.function.Function;

public interface CloseableIterator<T> extends Iterator<T>, ExceptionlessAutoCloseable {

	default <U> CloseableIterator<U> map(Function<T, U> mapper) {
		return new RemappedIterator<>(this, mapper);
	}

}
