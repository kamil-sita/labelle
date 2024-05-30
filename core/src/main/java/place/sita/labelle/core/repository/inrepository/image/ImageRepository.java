package place.sita.labelle.core.repository.inrepository.image;

import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Component;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilder;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilderWithRemoval;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.*;
import place.sita.labelle.jooq.tables.records.ImageFileRecord;
import place.sita.labelle.jooq.tables.records.ImageRecord;
import place.sita.labelle.jooq.tables.records.RootRecord;

import java.util.*;

import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Component
public class ImageRepository {

	private final DSLContext dslContext;

	public ImageRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}


	public <Self extends PreprocessableDataSourceWithRemoval<ImageResponse, FilteringApi<Self>, Self>> Self images() {
		return DataSourceBuilder.build(
			getUnderlyingDataSourceWIthRemoval(),
			this.<Self>getPreprocessingFactory(),
			getUnderlyingDataSourcePreprocessingAdapter(),
			getPreferredPageSizeProvider()
		);
	}

	private UnderlyingDataSourceWithRemoval<ImageResponse, PreprocessingType> getUnderlyingDataSourceWIthRemoval() {
		return JooqUnderlyingDataSourceBuilder.build(
			getJooqUnderlyingDataSourceBuilderWithRemoval(),
			() -> dslContext
		);
	}

	private static JooqUnderlyingDataSourceBuilderWithRemoval<ImageResponse, PreprocessingType> getJooqUnderlyingDataSourceBuilderWithRemoval() {
		return new JooqUnderlyingDataSourceBuilderWithRemoval<>() {

			private final TableField<ImageRecord, UUID> imageId = IMAGE.ID;
			private final TableField<RootRecord, String> rootDir = IMAGE.imageResolvable().imageFile().root().ROOT_DIR;
			private final TableField<ImageFileRecord, String> relativeDir = IMAGE.imageResolvable().imageFile().RELATIVE_DIR;

			@Override
			public Table<?> from(List<PreprocessingType> preprocessing) {
				return IMAGE;
			}

			@Override
			public RecordMapper<Record, ImageResponse> mapper() {
				return rr -> {
					return new ImageResponse(rr.get(imageId), rr.get(rootDir), rr.get(relativeDir));
				};
			}

			@Override
			public Collection<? extends SelectFieldOrAsterisk> select(List<PreprocessingType> preprocessing) {
				return List.of(imageId, rootDir, relativeDir);
			}

			@Override
			public Collection<? extends OrderField<?>> orderBy(List<PreprocessingType> preprocessing) {
				return List.of(imageId);
			}

			@Override
			public Collection<? extends Condition> where(List<PreprocessingType> preprocessing) {
				List<Condition> conditions = new ArrayList<>();
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof FilterByRepositoryPreprocessor filterByRepositoryPreprocessor) {
						conditions.add(IMAGE.REPOSITORY_ID.equal(filterByRepositoryPreprocessor.repositoryUuid));
					} else if (preprocessingType instanceof FilterByImageIdPreprocessor filterByImageIdPreprocessor) {
						conditions.add(IMAGE.ID.equal(filterByImageIdPreprocessor.imageUuid));
					} else {
						throw new IllegalStateException();
					}
				}
				return conditions;
			}
		};
	}

	private static PreferredPageSizeProvider getPreferredPageSizeProvider() {
		return new PreferredPageSizeProvider() {
			@Override
			public int preferredPageSize() {
				return 50;
			}
		};
	}

	private static UnderlyingDataSourcePreprocessingAdapter<PreprocessingType> getUnderlyingDataSourcePreprocessingAdapter() {
		return new UnderlyingDataSourcePreprocessingAdapter<PreprocessingType>() {
			@Override
			public PreprocessingType getPage(Page page) {
				return new PagingPreprocessor(page);
			}
		};
	}

	private <Self extends PreprocessableDataSourceWithRemoval<ImageResponse, FilteringApi<Self>, Self>> PreprocessingFactory<FilteringApi<Self>, PreprocessingType, Self> getPreprocessingFactory() {
		return new PreprocessingFactory<>() {
			@Override
			public FilteringApi<Self> createExposeableApi(PreprocessingApiAdapter<PreprocessingType, Self> adapter) {
				return new FilteringApi<>() {
					@Override
					public Self filterByRepository(UUID repositoryUuid) {
						return adapter.accept(new FilterByRepositoryPreprocessor(repositoryUuid));
					}

					@Override
					public Self filterByImageId(UUID imageUuid) {
						return adapter.accept(new FilterByImageIdPreprocessor(imageUuid));
					}
				};
			}
		};
	}

	private sealed interface PreprocessingType {

	}

	private record PagingPreprocessor(Page page) implements PreprocessingType {

	}

	private record FilterByRepositoryPreprocessor(UUID repositoryUuid) implements PreprocessingType {

	}

	private record FilterByImageIdPreprocessor(UUID imageUuid) implements PreprocessingType {

	}

	public interface FilteringApi<ReturnT> {

		ReturnT filterByRepository(UUID repositoryUuid);

		ReturnT filterByImageId(UUID imageUuid);

	}

	public List<ImageResponse> images(UUID repositoryUuid, int offset, int limit, String query) {
		return dslContext
			.select(IMAGE.ID, IMAGE.imageResolvable().imageFile().RELATIVE_DIR, IMAGE.imageResolvable().imageFile().root().ROOT_DIR)
			.from(IMAGE)
			.where(IMAGE.REPOSITORY_ID.equal(repositoryUuid))
			.orderBy(IMAGE.ID)
			.limit(limit)
			.offset(offset)
			.fetch()
			.map(rr -> {
				return new ImageResponse(rr.value1(), rr.value3(), rr.value2());
			});
	}

	public Optional<ImageResponse> loadImage(UUID imageId) {
		return dslContext
			.select(IMAGE.ID, IMAGE.imageResolvable().imageFile().RELATIVE_DIR, IMAGE.imageResolvable().imageFile().root().ROOT_DIR)
			.from(IMAGE)
			.where(IMAGE.ID.eq(imageId))
			.fetchOptional()
			.map(rr -> {
				return new ImageResponse(rr.value1(), rr.value3(), rr.value2());
			});
	}

}
