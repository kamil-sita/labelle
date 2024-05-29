package place.sita.labelle.datasource;

import java.util.Iterator;

public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
	@Override
	void close();
}
