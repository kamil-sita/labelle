package place.sita.labelle.core.repository.inrepository.image;

import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Component;
import place.sita.labelle.datasource.cross.PreprocessableIdDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.cross.UnderlyingIdDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilder;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.*;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilderWithRemovalAndId;
import place.sita.labelle.datasource.impl.jooq.TableFieldAndValue;
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


	public <Self extends PreprocessableIdDataSourceWithRemoval<UUID, ImageResponse, FilteringApi<Self>, Self>> Self images() {
		return DataSourceBuilder.build(
			getUnderlyingDataSourceWIthRemoval(),
			this.<Self>getPreprocessingFactory(),
			getUnderlyingDataSourcePreprocessingAdapter(),
			getPreferredPageSizeProvider(),
			byIdPreprocessingAdapter()
		);
	}

	private UnderlyingIdDataSourceWithRemoval<UUID, ImageResponse, PreprocessingType> getUnderlyingDataSourceWIthRemoval() {
		return JooqUnderlyingDataSourceBuilder.build(
			getJooqUnderlyingDataSourceBuilderWithIdAndRemoval(),
			() -> dslContext
		);
	}

	private ByIdPreprocessingAdapter<UUID, PreprocessingType> byIdPreprocessingAdapter() {
		return new ByIdPreprocessingAdapter<UUID, PreprocessingType>() {
			@Override
			public PreprocessingType accept(Collection<UUID> uuids) {
				return new FilterByImageIdsPreprocessor(new ArrayList<>(uuids));
			}
		};
	}

	private static JooqUnderlyingDataSourceBuilderWithRemovalAndId<UUID, ImageResponse, PreprocessingType> getJooqUnderlyingDataSourceBuilderWithIdAndRemoval() {
		return new JooqUnderlyingDataSourceBuilderWithRemovalAndId<>() {

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
				return List.of(rootDir, relativeDir, imageId);
			}

			@Override
			public Collection<TableFieldAndValue> deconstructByOrder(ImageResponse imageResponse, List<PreprocessingType> preprocessing) {
				return List.of(new TableFieldAndValue<>(rootDir, imageResponse.root()), new TableFieldAndValue(relativeDir, imageResponse.path()), new TableFieldAndValue(imageId, imageResponse.id()));
			}

			@Override
			public Collection<? extends Condition> where(List<PreprocessingType> preprocessing) {
				List<Condition> conditions = new ArrayList<>();
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof FilterByRepositoryPreprocessor filterByRepositoryPreprocessor) {
						conditions.add(IMAGE.REPOSITORY_ID.equal(filterByRepositoryPreprocessor.repositoryUuid));
					} else if (preprocessingType instanceof FilterByImageIdsPreprocessor filterByImageIdsPreprocessor) {
						conditions.add(IMAGE.ID.in(filterByImageIdsPreprocessor.imageIds));
					} else if (preprocessingType instanceof PagingPreprocessor pagingPreprocessor) {
						// no op
					} else {
						throw new IllegalStateException();
					}

				}
				return conditions;
			}

			@Override
			public Integer limit(List<PreprocessingType> preprocessing) {
				Integer limit = null;
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof FilterByRepositoryPreprocessor filterByRepositoryPreprocessor) {
						// no op
					} else if (preprocessingType instanceof FilterByImageIdsPreprocessor filterByImageIdsPreprocessor) {
						// no op
					} else if (preprocessingType instanceof PagingPreprocessor pagingPreprocessor) {
						if (limit != null) {
							throw new IllegalStateException();
						}
						limit = pagingPreprocessor.page().limit();
					} else {
						throw new IllegalStateException();
					}

				}
				return limit;
			}

			@Override
			public Integer offset(List<PreprocessingType> preprocessing) {
				Integer offset = null;
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof FilterByRepositoryPreprocessor filterByRepositoryPreprocessor) {
						// no op
					} else if (preprocessingType instanceof FilterByImageIdsPreprocessor filterByImageIdsPreprocessor) {
						// no op
					} else if (preprocessingType instanceof PagingPreprocessor pagingPreprocessor) {
						if (offset != null) {
							throw new IllegalStateException();
						}
						offset = pagingPreprocessor.page().offset();
					} else {
						throw new IllegalStateException();
					}

				}
				return offset;
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
						return adapter.accept(new FilterByImageIdsPreprocessor(List.of(imageUuid)));
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

	private record FilterByImageIdsPreprocessor(List<UUID> imageIds) implements PreprocessingType {

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
