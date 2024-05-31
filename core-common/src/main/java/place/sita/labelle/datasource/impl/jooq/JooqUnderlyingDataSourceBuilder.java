package place.sita.labelle.datasource.impl.jooq;

import org.jooq.*;
import org.jooq.Record;
import place.sita.labelle.datasource.impl.UnderlyingDataSourceWithRemoval;
import place.sita.labelle.datasource.util.CloseableIterator;
import place.sita.labelle.datasource.impl.UnderlyingDataSource;

import java.util.*;

public class JooqUnderlyingDataSourceBuilder {

	private JooqUnderlyingDataSourceBuilder() {

	}

	public static <Type, AcceptedPreprocessingType> UnderlyingDataSource<Type, AcceptedPreprocessingType> build(
		JooqUnderlyingDataSourceBuilderWithoutRemoval<Type, AcceptedPreprocessingType> queryBuilder,
		JooqContextProvider contextProvider
	) {
		return new UnderlyingDataSourceImplWithoutRemoval<>(queryBuilder, contextProvider);
	}

	public static <Type, AcceptedPreprocessingType> UnderlyingDataSourceWithRemoval<Type, AcceptedPreprocessingType> build(
		JooqUnderlyingDataSourceBuilderWithRemoval<Type, AcceptedPreprocessingType> queryBuilder,
		JooqContextProvider contextProvider
	) {
		return new UnderlyingDataSourceImplWithRemoval<>(queryBuilder, contextProvider);
	}

	private static class UnderlyingDataSourceImplWithRemoval<Type, AcceptedProcessingType> extends UnderlyingDataSourceImpl<Type, AcceptedProcessingType> implements UnderlyingDataSourceWithRemoval<Type, AcceptedProcessingType> {
		private final JooqUnderlyingDataSourceBuilderWithRemoval<Type, AcceptedProcessingType> queryBuilder;
		private final JooqContextProvider contextProvider;

		private UnderlyingDataSourceImplWithRemoval(JooqUnderlyingDataSourceBuilderWithRemoval<Type, AcceptedProcessingType> queryBuilder, JooqContextProvider contextProvider) {
			super(queryBuilder, contextProvider);
			this.queryBuilder = queryBuilder;
			this.contextProvider = contextProvider;
		}

		@Override
		protected SelectJoinStep<Record> applyFiltering(SelectSelectStep<Record> step, List<AcceptedProcessingType> preprocessing) {
			return step.from(queryBuilder.from(preprocessing));
		}

		@Override
		public void remove(List<AcceptedProcessingType> preprocessing) {
			if (queryBuilder.offset(preprocessing) != null) {
				throw new UnsupportedOperationException("Cannot remove with offset");
			}

			contextProvider.getContext()
				.deleteFrom(queryBuilder.from(preprocessing))
				.where(queryBuilder.where(preprocessing))
				.limit(queryBuilder.limit(preprocessing))
				.execute();
		}

	}

	private static class UnderlyingDataSourceImplWithoutRemoval<Type, AcceptedProcessingType> extends UnderlyingDataSourceImpl<Type, AcceptedProcessingType> {

		private final JooqUnderlyingDataSourceBuilderWithoutRemoval<Type, AcceptedProcessingType> queryBuilder;

		private UnderlyingDataSourceImplWithoutRemoval(JooqUnderlyingDataSourceBuilderWithoutRemoval<Type, AcceptedProcessingType> queryBuilder, JooqContextProvider contextProvider) {
			super(queryBuilder, contextProvider);
			this.queryBuilder = queryBuilder;
		}

		@Override
		protected SelectJoinStep<Record> applyFiltering(SelectSelectStep<Record> step, List<AcceptedProcessingType> preprocessing) {
			return step.from(queryBuilder.from(preprocessing));
		}
	}

	private abstract static class UnderlyingDataSourceImpl<Type, AcceptedPreprocessingType> implements UnderlyingDataSource<Type, AcceptedPreprocessingType> {


		private final JooqUnderlyingDataSourceQueryBuilder<Type, AcceptedPreprocessingType> queryBuilder;
		private final JooqContextProvider contextProvider;

		private UnderlyingDataSourceImpl(JooqUnderlyingDataSourceQueryBuilder<Type, AcceptedPreprocessingType> queryBuilder, JooqContextProvider contextProvider) {
			this.queryBuilder = queryBuilder;
			this.contextProvider = contextProvider;
		}

		protected abstract SelectJoinStep<Record> applyFiltering(SelectSelectStep<Record> step, List<AcceptedPreprocessingType> preprocessing);

		@Override
		public CloseableIterator<Type> get(List<AcceptedPreprocessingType> preprocessing, int pageSize) {
			SelectSelectStep<Record> preFrom = contextProvider.getContext()
				.select(queryBuilder.select(preprocessing));

			SelectJoinStep<Record> afterFrom = applyFiltering(preFrom, preprocessing);

			Cursor<Record> cursor = afterFrom
				.where(queryBuilder.where(preprocessing))
				.orderBy(queryBuilder.orderBy(preprocessing))
				.limit(queryBuilder.limit(preprocessing))
				.offset(queryBuilder.offset(preprocessing))
				.fetchLazy();

			Queue<Record> buffer = new ArrayDeque<>(pageSize);

			CloseableIterator<Record> iterator = new CloseableIterator<>() {
				boolean bufferHasAllTheDataLeft = false;

				@Override
				public void close() {
					cursor.close();
				}

				@Override
				public boolean hasNext() {
					if (bufferHasAllTheDataLeft) {
						return !buffer.isEmpty();
					}
					if (buffer.isEmpty()) {
						List<Record> next = cursor.fetchNext(pageSize);
						if (next.size() < pageSize) {
							bufferHasAllTheDataLeft = true;
						}
						buffer.addAll(next);
					}
					return !buffer.isEmpty();
				}

				@Override
				public Record next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					return buffer.poll();
				}
			};

			return iterator.map(queryBuilder.mapper());
		}

		@Override
		public int count(List<AcceptedPreprocessingType> preprocessing) {
			SelectSelectStep<Record> preFrom = (SelectSelectStep<Record>) (SelectSelectStep) contextProvider.getContext()
				.selectCount();

			SelectJoinStep<Record1<Integer>> afterFrom = (SelectJoinStep<Record1<Integer>>) (SelectJoinStep) applyFiltering(preFrom, preprocessing);

			return
				afterFrom
				.where(queryBuilder.where(preprocessing))
				.limit(queryBuilder.limit(preprocessing))
				.offset(queryBuilder.offset(preprocessing))
				.fetchOne()
				.component1();
		}
	}

}
