package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.CloseableIterator;
import place.sita.labelle.datasource.DataSource;
import place.sita.labelle.datasource.NonUniqueAnswerException;
import place.sita.labelle.datasource.Page;

import java.util.List;
import java.util.Optional;

public class DataSourceBuilder {

	private DataSourceBuilder() {

	}

	public static <T, Self extends DataSource<T, Self>> Self listBackedDataSource(List<T> list) {

		return (Self) new DataSource<T, Self>() {

			@Override
			public Optional<T> getOneOptional() {
				if (list.size() > 1) {
					throw new NonUniqueAnswerException();
				} else {
					return list.stream().findFirst();
				}
			}

			@Override
			public Optional<T> getAnyOptional() {
				return list.stream().findAny();
			}

			@Override
			public CloseableIterator<T> getIterator(int pageSize) {
				return new CloseableIterator<>() {

					private int index = 0;

					@Override
					public void close() {
						// Do nothing
					}

					@Override
					public boolean hasNext() {
						return index < list.size();
					}

					@Override
					public T next() {
						return list.get(index++);
					}
				};
			}

			@Override
			public Self getPage(Page page) {
				List<T> sublist = list.subList(page.offset(), page.offset() + page.limit());
				return listBackedDataSource(sublist);
			}
		};

	}

}
