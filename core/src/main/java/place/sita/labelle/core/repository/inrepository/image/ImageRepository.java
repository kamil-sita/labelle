package place.sita.labelle.core.repository.inrepository.image;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import place.sita.labelle.datasource.IllegalApiUseException;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.datasource.cross.PreprocessableDataSourceWithRemoval;
import place.sita.labelle.datasource.cross.PreprocessableIdDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.*;
import place.sita.labelle.datasource.impl.cross.UnderlyingIdDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilder;
import place.sita.labelle.datasource.impl.jooq.JooqUnderlyingDataSourceBuilderWithRemovalAndId;
import place.sita.labelle.datasource.impl.jooq.TableFieldAndValue;
import place.sita.labelle.datasource.impl.jooq.binding.JooqFilteringVisitor;
import place.sita.labelle.datasource.impl.jooq.binding.JooqPropertyBindings;
import place.sita.labelle.datasource.impl.jooq.binding.LogicalPath;
import place.sita.labelle.jooq.Tables;
import place.sita.labelle.jooq.tables.records.ImageFileRecord;
import place.sita.labelle.jooq.tables.records.ImageRecord;
import place.sita.labelle.jooq.tables.records.RootRecord;
import place.sita.tflang.TFLangLexer;
import place.sita.tflang.TFLangParser;
import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.filteringexpression.parsing.TFlangFilteringExpressionParser;

import java.util.*;

import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Component
public class ImageRepository {

	private final DSLContext dslContext;

	public ImageRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public static FilteringExpression parse(String query, ANTLRErrorListener errorListener) {
		CharStream charStream = CharStreams.fromString(query);

		TFLangLexer lexer = new TFLangLexer(charStream);
		lexer.removeErrorListeners();
		if (errorListener != null) {
			lexer.addErrorListener(errorListener);
		}

		TokenStream tokenStream = new CommonTokenStream(lexer);
		TFLangParser parser = new TFLangParser(tokenStream);
		parser.removeErrorListeners();
		if (errorListener != null) {
			parser.addErrorListener(errorListener);
		}

		ParseTree parseTree = parser.parseMatchExpression();

		return new TFlangFilteringExpressionParser().visit(parseTree);
	}

	public static Condition parseToCondition(String query, ANTLRErrorListener errorListener) {
		FilteringExpression filteringExpression = parse(query, errorListener);
		return new JooqFilteringVisitor(List.of(), bindings()).visit(filteringExpression);
	}

	public static JooqPropertyBindings bindings() {
		JooqPropertyBindings bindings = new JooqPropertyBindings();
		bindings.addTable(LogicalPath.path("tags"), Tables.IMAGE_TAGS);
		bindings.addTableJoin(LogicalPath.path("tags"), Tables.IMAGE_TAGS.IMAGE_ID.eq(IMAGE.ID));
		bindings.addBinding(LogicalPath.path("tags", "tag"), Tables.IMAGE_TAGS.TAG);
		bindings.addBinding(LogicalPath.path("tags", "category"), Tables.IMAGE_TAGS.TAG_CATEGORY);
		bindings.addBinding(LogicalPath.path("path"), DSL.concat(IMAGE.imageResolvable().imageFile().root().ROOT_DIR, DSL.val("/"), IMAGE.imageResolvable().imageFile().RELATIVE_DIR));
		return bindings;
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
					switch (preprocessingType) {
						case FilterByRepositoryPreprocessor filterByRepositoryPreprocessor ->
							conditions.add(IMAGE.REPOSITORY_ID.equal(filterByRepositoryPreprocessor.repositoryUuid));
						case FilterByImageIdsPreprocessor filterByImageIdsPreprocessor ->
							conditions.add(IMAGE.ID.in(filterByImageIdsPreprocessor.imageIds));
						case PagingPreprocessor pagingPreprocessor -> {
							// no op
						}
						case FilterUsingTfLang filterUsingTfLang -> {
							String query = filterUsingTfLang.query;
							if (query != null && !query.isBlank()) {
								conditions.add(parseToCondition(filterUsingTfLang.query(), null));
							}
						}
					}

				}
				return conditions;
			}

			@Override
			public Integer limit(List<PreprocessingType> preprocessing) {
				Integer limit = null;
				for (PreprocessingType preprocessingType : preprocessing) {
					switch (preprocessingType) {
						case FilterByRepositoryPreprocessor filterByRepositoryPreprocessor -> { /* no op */ }
						case FilterByImageIdsPreprocessor filterByImageIdsPreprocessor -> { /* no op */ }
						case PagingPreprocessor pagingPreprocessor -> {
							if (limit != null) {
								throw new IllegalApiUseException("Limit redefinition");
							}
							limit = pagingPreprocessor.page().limit();
						}
						case FilterUsingTfLang filterUsingTfLang -> {
						}
					}

				}
				return limit;
			}

			@Override
			public Integer offset(List<PreprocessingType> preprocessing) {
				Integer offset = null;
				for (PreprocessingType preprocessingType : preprocessing) {
					switch (preprocessingType) {
						case FilterByRepositoryPreprocessor filterByRepositoryPreprocessor -> { /* no op */ }
						case FilterByImageIdsPreprocessor filterByImageIdsPreprocessor -> { /* no op */ }
						case PagingPreprocessor pagingPreprocessor -> {
							if (offset != null) {
								throw new IllegalApiUseException("Offset redefinition");
							}
							offset = pagingPreprocessor.page().offset();
						}
						case FilterUsingTfLang filterUsingTfLang -> {
						}
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

					@Override
					public Self filterUsingTfLang(String query) {
						return adapter.accept(new FilterUsingTfLang(query));
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

	private record FilterUsingTfLang(String query) implements PreprocessingType {

	}

	public interface FilteringApi<ReturnT> {

		ReturnT filterByRepository(UUID repositoryUuid);

		ReturnT filterByImageId(UUID imageUuid);

		ReturnT filterUsingTfLang(String query);

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
