package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.util.CloseableIterator;

import java.util.List;

public interface UnderlyingDataSourceWithRemoval<Type, AcceptedPreprocessingType> extends UnderlyingDataSource<Type, AcceptedPreprocessingType> {

	default void remove(List<AcceptedPreprocessingType> preprocessing) {
		try (CloseableIterator<Type> iterator = get(preprocessing, 1)) {
			while (iterator.hasNext()) {
				iterator.remove();
				iterator.next();
			}
		}
	}

}
