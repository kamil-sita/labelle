package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.CloseableIterator;

import java.util.List;

public interface UnderlyingDataSource<Type, AcceptedPreprocessingType> {

	CloseableIterator<Type> get(List<AcceptedPreprocessingType> preprocessing, int pageSize);

	default void remove(List<AcceptedPreprocessingType> preprocessing) {
		try (CloseableIterator<Type> iterator = get(preprocessing, 1)) {
			while (iterator.hasNext()) {
				iterator.remove();
				iterator.next();
			}
		}
	}

	default int count(List<AcceptedPreprocessingType> preprocessing) {
		int count = 0;
		try (CloseableIterator<Type> iterator = get(preprocessing, 1)) {
			while (iterator.hasNext()) {
				iterator.next();
				count++;
			}
		}
		return count;
	}

}
