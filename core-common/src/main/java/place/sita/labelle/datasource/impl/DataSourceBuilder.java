package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.CloseableIterator;
import place.sita.labelle.datasource.NonUniqueAnswerException;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataSourceBuilder {

	private DataSourceBuilder() {

	}

	public static <T, ProcessorApi, UnderstandablePreprocessingInfo, Self extends PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self>> Self preprocessableDataSourceWithRemoval(
			UnderlyingDataSource<T, UnderstandablePreprocessingInfo> underlyingDataSource,
			PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory,
			UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter,
			PreferredPageSizeProvider preferredPageSizeProvider) {

		List<UnderstandablePreprocessingInfo> context = new ArrayList<>();

		return preprocessableDataSourceWithRemovalWithContext(adapter, underlyingDataSource, preprocessingFactory, context, preferredPageSizeProvider);
	}

	private static <T, ProcessorApi, UnderstandablePreprocessingInfo, Self extends PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self>> Self preprocessableDataSourceWithRemovalWithContext(
		UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter,
		UnderlyingDataSource<T, UnderstandablePreprocessingInfo> underlyingDataSource,
		PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory,
		List<UnderstandablePreprocessingInfo> context, PreferredPageSizeProvider preferredPageSizeProvider) {
		PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self> dataSource = new PreprocessableDataSourceWithRemoval<>() {

			@Override
			public Optional<T> getOneOptional() throws NonUniqueAnswerException {
				Self self = getPage(new Page(0, 2));
				List<T> firstTwo = self.getAll();
				if (firstTwo.size() == 1) {
					return Optional.of(firstTwo.get(0));
				} else if (firstTwo.size() > 1) {
					throw new NonUniqueAnswerException();
				}
				return Optional.empty();
			}

			@Override
			public Optional<T> getAnyOptional() {
				Self self = getPage(new Page(0, 1));
				List<T> firstTwo = self.getAll();
				if (!firstTwo.isEmpty()) {
					return Optional.of(firstTwo.get(0));
				}
				return Optional.empty();
			}

			@Override
			public List<T> getAll() {
				List<T> elements = new ArrayList<>();
				try (CloseableIterator<T> iterator = getIterator()) {
					while (iterator.hasNext()) {
						elements.add(iterator.next());
					}
				}
				return elements;
			}

			@Override
			public int count() {
				return underlyingDataSource.count(context);
			}

			@Override
			public CloseableIterator<T> getIterator() {
				int pageSize = preferredPageSizeProvider.preferredPageSize();
				return getIterator(pageSize);
			}

			@Override
			public CloseableIterator<T> getIterator(int pageSize) {
				return underlyingDataSource.get(context, pageSize);
			}

			@Override
			public Self getPage(Page page) {
				UnderstandablePreprocessingInfo processingInfo = adapter.getPage(page);
				List<UnderstandablePreprocessingInfo> myContext = new ArrayList<>(context);
				myContext.add(processingInfo);
				return preprocessableDataSourceWithRemovalWithContext(adapter, underlyingDataSource, preprocessingFactory, myContext, preferredPageSizeProvider);
			}

			@Override
			public ProcessorApi process() {
				PreprocessingApiAdapter<UnderstandablePreprocessingInfo, Self> preprocessingApiAdapter = new PreprocessingApiAdapter<>() {
					@Override
					public Self accept(UnderstandablePreprocessingInfo understandablePreprocessingInfo) {
						List<UnderstandablePreprocessingInfo> myContext = new ArrayList<>(context);
						myContext.add(understandablePreprocessingInfo);
						return preprocessableDataSourceWithRemovalWithContext(adapter, underlyingDataSource, preprocessingFactory, myContext, preferredPageSizeProvider);
					}
				};

				return preprocessingFactory.createExposeableApi(preprocessingApiAdapter);
			}

			@Override
			public void remove() {
				underlyingDataSource.remove(context);
			}
		};

		return (Self) dataSource;
	}
}
