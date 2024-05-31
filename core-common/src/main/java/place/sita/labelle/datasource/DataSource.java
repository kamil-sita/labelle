package place.sita.labelle.datasource;

import place.sita.labelle.datasource.util.CloseableIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface DataSource<T, Self extends DataSource<T, Self>> {

	default T getOne() throws NonUniqueAnswerException {
		return getOneOptional().orElse(null);
	}

	Optional<T> getOneOptional() throws NonUniqueAnswerException;

	default T getAny() {
		return getAnyOptional().orElse(null);
	}

	Optional<T> getAnyOptional();

	default List<T> getAll() {
		List<T> list = new ArrayList<>();
		try (CloseableIterator<T> iterator = getIterator()) {
			iterator.forEachRemaining(list::add);
		}
		return list;
	}

	default CloseableIterator<T> getIterator() {
		return getIterator(100);
	}

	CloseableIterator<T> getIterator(int pageSize);

	Self getPage(Page page);

	default int count() {
		return getAll().size();
	}

	default void forEach(Consumer<T> consumer) {
		try (CloseableIterator<T> iterator = getIterator()) {
			iterator.forEachRemaining(consumer);
		}
	}

}
