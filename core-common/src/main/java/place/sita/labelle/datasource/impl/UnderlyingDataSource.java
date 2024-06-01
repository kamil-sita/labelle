package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.util.CloseableIterator;

import java.util.List;

public interface UnderlyingDataSource<Type, AcceptedPreprocessingType> {

	CloseableIterator<Type> get(List<AcceptedPreprocessingType> preprocessing, int pageSize);

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
