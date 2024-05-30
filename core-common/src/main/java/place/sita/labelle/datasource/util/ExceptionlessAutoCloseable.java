package place.sita.labelle.datasource.util;

public interface ExceptionlessAutoCloseable extends AutoCloseable {
	@Override
	void close();
}
