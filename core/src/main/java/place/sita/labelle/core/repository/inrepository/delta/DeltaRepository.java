package place.sita.labelle.core.repository.inrepository.delta;

import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.image.ImageRepository;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.*;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilder;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilderWithRemoval;
import place.sita.labelle.jooq.tables.ImageDelta;
import place.sita.labelle.jooq.tables.records.TagDeltaRecord;

import java.util.*;

import static place.sita.labelle.jooq.tables.TagDelta.TAG_DELTA;

@Component
public class DeltaRepository {

	private final DSLContext dslContext;
	private final ImageRepository imageRepository;

	public DeltaRepository(DSLContext dslContext, ImageRepository imageRepository) {
		this.dslContext = dslContext;
		this.imageRepository = imageRepository;
	}

	public List<TagDeltaResponse> getTagDeltas(UUID imageId) {
		return dslContext.select(TAG_DELTA.ADDS, TAG_DELTA.TAG, TAG_DELTA.CATEGORY)
			.from(TAG_DELTA)
			.where(TAG_DELTA.IMAGE_ID.eq(imageId))
			.fetch()
			.map(rr -> {
				return new TagDeltaResponse(rr.value3(), rr.value2(), rr.value1() ? TagDeltaType.ADD : TagDeltaType.REMOVE);
			});
	}

	public Optional<ImageResponse> getImageDelta(UUID imageId) {
		return dslContext.select(ImageDelta.IMAGE_DELTA.IMAGE_RESOLVABLE_ID)
			.from(ImageDelta.IMAGE_DELTA)
			.where(ImageDelta.IMAGE_DELTA.IMAGE_ID.eq(imageId))
			.fetchOptional()
			.map(rr -> {
				return imageRepository.loadImage(rr.value1()).get();
			});
	}

	public <Self extends PreprocessableDataSourceWithRemoval<TagDeltaResponse, FilteringApi<Self>, Self>> Self tagDeltas() {
		return DataSourceBuilder.build(
			getUnderlyingDataSourceWIthRemoval(),
			this.<Self>getPreprocessingFactory(),
			getUnderlyingDataSourcePreprocessingAdapter(),
			getPreferredPageSizeProvider()
		);
	}

	private UnderlyingDataSourceWithRemoval<TagDeltaResponse, PreprocessingType> getUnderlyingDataSourceWIthRemoval() {
		return JooqUnderlyingDataSourceBuilder.build(
			getJooqUnderlyingDataSourceBuilderWithRemoval(),
			() -> dslContext
		);
	}

	private static JooqUnderlyingDataSourceBuilderWithRemoval<TagDeltaResponse, PreprocessingType> getJooqUnderlyingDataSourceBuilderWithRemoval() {
		return new JooqUnderlyingDataSourceBuilderWithRemoval<>() {

			private final TableField<TagDeltaRecord, UUID> imageId = TAG_DELTA.IMAGE_ID;
			private final TableField<TagDeltaRecord, Boolean> adds = TAG_DELTA.ADDS;
			private final TableField<TagDeltaRecord, String> category = TAG_DELTA.CATEGORY;
			private final TableField<TagDeltaRecord, String> tag = TAG_DELTA.TAG;


			@Override
			public Table<?> from(List<PreprocessingType> preprocessing) {
				return TAG_DELTA;
			}

			@Override
			public RecordMapper<Record, TagDeltaResponse> mapper() {
				return rr -> {
					return new TagDeltaResponse(rr.get(category), rr.get(tag), rr.get(adds) ? TagDeltaType.ADD : TagDeltaType.REMOVE);
				};
			}

			@Override
			public Collection<? extends SelectFieldOrAsterisk> select(List<PreprocessingType> preprocessing) {
				return List.of(adds, category, tag);
			}

			@Override
			public Collection<? extends OrderField<?>> orderBy(List<PreprocessingType> preprocessing) {
				return List.of(imageId, category, tag);
			}

			@Override
			public Collection<? extends Condition> where(List<PreprocessingType> preprocessing) {
				List<Condition> conditions = new ArrayList<>();
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof FilterByImageIdPreprocessor filterByImageIdPreprocessor) {
						conditions.add(TAG_DELTA.IMAGE_ID.equal(filterByImageIdPreprocessor.imageUuid));
					}
					if (preprocessingType instanceof FilterByTagDeltaPreprocessor filterByTagDeltaPreprocessor) {
						conditions.add(TAG_DELTA.CATEGORY.equal(filterByTagDeltaPreprocessor.category));
						conditions.add(TAG_DELTA.TAG.equal(filterByTagDeltaPreprocessor.tag));
						conditions.add(TAG_DELTA.ADDS.equal(filterByTagDeltaPreprocessor.type == TagDeltaType.ADD));
					}
				}
				return conditions;
			}

			@Override
			public Integer limit(List<PreprocessingType> preprocessing) {
				Integer limit = null;
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof PagingPreprocessor pagingPreprocessor) {
						if (limit != null) {
							throw new IllegalStateException();
						}
						limit = pagingPreprocessor.page().limit();
					}
				}
				return limit;
			}

			@Override
			public Integer offset(List<PreprocessingType> preprocessing) {
				Integer offset = null;
				for (PreprocessingType preprocessingType : preprocessing) {
					if (preprocessingType instanceof PagingPreprocessor pagingPreprocessor) {
						if (offset != null) {
							throw new IllegalStateException();
						}
						offset = pagingPreprocessor.page().offset();
					}
				}
				return offset;
			}
		};
	}

	private <Self extends PreprocessableDataSourceWithRemoval<TagDeltaResponse, FilteringApi<Self>, Self>> PreprocessingFactory<FilteringApi<Self>, PreprocessingType, Self> getPreprocessingFactory() {
		return new PreprocessingFactory<>() {
			@Override
			public FilteringApi<Self> createExposeableApi(PreprocessingApiAdapter<PreprocessingType, Self> adapter) {
				return new FilteringApi<>() {
					@Override
					public Self filterByImageId(UUID imageUuid) {
						return adapter.accept(new FilterByImageIdPreprocessor(imageUuid));
					}

					@Override
					public Self byTagDelta(TagDeltaType deltaType, String category, String tag) {
						return adapter.accept(new FilterByTagDeltaPreprocessor(category, tag, deltaType));
					}
				};
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

	private sealed interface PreprocessingType {

	}

	private record PagingPreprocessor(Page page) implements PreprocessingType {

	}

	private record FilterByImageIdPreprocessor(UUID imageUuid) implements PreprocessingType {

	}

	private record FilterByTagDeltaPreprocessor(String category, String tag, TagDeltaType type) implements PreprocessingType {

	}

	public interface FilteringApi<ReturnT> {

		ReturnT filterByImageId(UUID imageUuid);

		ReturnT byTagDelta(TagDeltaType deltaType, String category, String tag);
	}
}
