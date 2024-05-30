package place.sita.labelle.datasource.util;

import java.util.function.Function;

public class RemappedIterator<OriginalType, NewType> implements CloseableIterator<NewType> {

	private final CloseableIterator<OriginalType> originalIterator;
	private final Function<OriginalType, NewType> mapper;

	public RemappedIterator(CloseableIterator<OriginalType> originalIterator, Function<OriginalType, NewType> mapper) {
		this.originalIterator = originalIterator;
		this.mapper = mapper;
	}

	@Override
	public boolean hasNext() {
		return originalIterator.hasNext();
	}

	@Override
	public NewType next() {
		return mapper.apply(originalIterator.next());
	}

	@Override
	public void close() {
		originalIterator.close();
	}
}
