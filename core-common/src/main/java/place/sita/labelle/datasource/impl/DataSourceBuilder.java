package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.PreprocessableDataSource;
import place.sita.labelle.datasource.util.CloseableIterator;
import place.sita.labelle.datasource.NonUniqueAnswerException;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataSourceBuilder {

	private DataSourceBuilder() {

	}

	public static <T, ProcessorApi, UnderstandablePreprocessingInfo, Self extends PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self>> Self build(
			UnderlyingDataSourceWithRemoval<T, UnderstandablePreprocessingInfo> underlyingDataSource,
			PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory,
			UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter,
			PreferredPageSizeProvider preferredPageSizeProvider) {

		List<UnderstandablePreprocessingInfo> context = new ArrayList<>();

		return (Self) new PreprocessableDataSourceWithRemovalImpl(adapter, underlyingDataSource, preprocessingFactory, context, preferredPageSizeProvider);
	}

	public static <T, ProcessorApi, UnderstandablePreprocessingInfo, Self extends PreprocessableDataSource<T, ProcessorApi, Self>> Self build(
			UnderlyingDataSource<T, UnderstandablePreprocessingInfo> underlyingDataSource,
			PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory,
			UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter,
			PreferredPageSizeProvider preferredPageSizeProvider) {

		List<UnderstandablePreprocessingInfo> context = new ArrayList<>();

		return (Self) new PreprocessableDataSourceImpl(adapter, underlyingDataSource, preprocessingFactory, context, preferredPageSizeProvider);
	}

	private static class PreprocessableDataSourceWithRemovalImpl<T, ProcessorApi, UnderstandablePreprocessingInfo, Self extends PreprocessableDataSourceWithRemovalImpl<T, ProcessorApi, UnderstandablePreprocessingInfo, Self>>
		extends DataSourceBuilder.PreprocessableDataSourceImpl<T, ProcessorApi, UnderstandablePreprocessingInfo, Self>
		implements PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self> {

		protected final UnderlyingDataSourceWithRemoval<T, UnderstandablePreprocessingInfo> underlyingDataSource;

		private PreprocessableDataSourceWithRemovalImpl(
			UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter,
			UnderlyingDataSourceWithRemoval<T, UnderstandablePreprocessingInfo> underlyingDataSource,
			PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory,
			List<UnderstandablePreprocessingInfo> context,
			PreferredPageSizeProvider preferredPageSizeProvider) {
			super(adapter, underlyingDataSource, preprocessingFactory, context, preferredPageSizeProvider);
			this.underlyingDataSource = underlyingDataSource;
		}

		@Override
		public void remove() {
			underlyingDataSource.remove(context);
		}

		@Override
		protected Self buildSelfWithProcessingInfo(UnderstandablePreprocessingInfo processingInfo) {
			List<UnderstandablePreprocessingInfo> myContext = new ArrayList<>(context);
			myContext.add(processingInfo);
			return (Self) new PreprocessableDataSourceWithRemovalImpl<>(adapter, underlyingDataSource, preprocessingFactory, myContext, preferredPageSizeProvider);
		}
	}

	private static class PreprocessableDataSourceImpl<T, ProcessorApi, UnderstandablePreprocessingInfo, Self extends PreprocessableDataSourceImpl<T, ProcessorApi, UnderstandablePreprocessingInfo, Self>>
		implements PreprocessableDataSource<T, ProcessorApi, Self> {

		protected final UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter;
		protected final UnderlyingDataSource<T, UnderstandablePreprocessingInfo> underlyingDataSource;
		protected final PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory;
		protected final List<UnderstandablePreprocessingInfo> context;
		protected final PreferredPageSizeProvider preferredPageSizeProvider;

		private PreprocessableDataSourceImpl(UnderlyingDataSourcePreprocessingAdapter<UnderstandablePreprocessingInfo> adapter,
		                                     UnderlyingDataSource<T, UnderstandablePreprocessingInfo> underlyingDataSource,
		                                     PreprocessingFactory<ProcessorApi, UnderstandablePreprocessingInfo, Self> preprocessingFactory,
		                                     List<UnderstandablePreprocessingInfo> context,
		                                     PreferredPageSizeProvider preferredPageSizeProvider) {
			this.adapter = adapter;
			this.underlyingDataSource = underlyingDataSource;
			this.preprocessingFactory = preprocessingFactory;
			this.context = context;
			this.preferredPageSizeProvider = preferredPageSizeProvider;
		}


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
			return buildSelfWithProcessingInfo(processingInfo);
		}

		@Override
		public ProcessorApi process() {
			PreprocessingApiAdapter<UnderstandablePreprocessingInfo, Self> preprocessingApiAdapter = new PreprocessingApiAdapter<>() {
				@Override
				public Self accept(UnderstandablePreprocessingInfo understandablePreprocessingInfo) {
					return buildSelfWithProcessingInfo(understandablePreprocessingInfo);
				}
			};

			return preprocessingFactory.createExposeableApi(preprocessingApiAdapter);
		}

		protected Self buildSelfWithProcessingInfo(UnderstandablePreprocessingInfo processingInfo) {
			List<UnderstandablePreprocessingInfo> myContext = new ArrayList<>(context);
			myContext.add(processingInfo);
			return (Self) new PreprocessableDataSourceImpl<>(adapter, underlyingDataSource, preprocessingFactory, myContext, preferredPageSizeProvider);
		}
	}
}
