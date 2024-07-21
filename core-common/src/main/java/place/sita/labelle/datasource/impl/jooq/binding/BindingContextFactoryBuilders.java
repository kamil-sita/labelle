package place.sita.labelle.datasource.impl.jooq.binding;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import place.sita.labelle.datasource.impl.jooq.binding.binding.BindingContextFactory;
import place.sita.labelle.datasource.impl.jooq.binding.binding.PropertyBindingContext;
import place.sita.labelle.datasource.impl.jooq.binding.binding.PropertyBindingSubEntityContext;
import place.sita.labelle.datasource.impl.jooq.binding.builder.BindingContextFactoryBuilder;
import place.sita.labelle.datasource.impl.jooq.binding.builder.ParentBindingContextFactoryBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BindingContextFactoryBuilders {

	public <TableT extends Table> BindingContextFactoryBuilder<TableT> forTable(TableT table) {
		return new FactoryBuilderClass<>(new UnderlyingContext(), new AtomicBoolean(true), true);
	}

	private static class FactoryBuilderClass<TableT extends Table> implements ParentBindingContextFactoryBuilder<TableT> {
		private final UnderlyingContext underlyingContext;
		private final AtomicBoolean buildContextOpen;
		private final boolean isParent;

		private FactoryBuilderClass(UnderlyingContext underlyingContext, AtomicBoolean buildContextOpen, boolean isParent) {
			this.underlyingContext = underlyingContext;
			this.buildContextOpen = buildContextOpen;
			this.isParent = isParent;
		}

		@Override
		public BindingContextFactory<TableT> build() {
			if (!isParent) {
				throw new BadApiUsageException("Cannot build a non-parent context");
			}
			buildContextOpen.set(false);
			return new BindContextFactoryImpl<>(underlyingContext);
		}

		@Override
		public void addField(String path, Function<TableT, Field> field) {
			assertContextOpen();
			if (underlyingContext.paths.contains(path)) {
				throw new ReusedPathException("Path already exists: " + path);
			}
			underlyingContext.paths.add(path);
			underlyingContext.fieldFactory.put(path, (Function<Table, Field>) field);
		}

		@Override
		public <NewTableT extends Table> BindingContextFactoryBuilder<NewTableT> addSubEntity(String path, Function<TableT, NewTableT> table) {
			return addSubEntity(path, table, (parent, child) -> null);
		}

		@Override
		public <NewTableT extends Table> BindingContextFactoryBuilder<NewTableT> addSubEntity(String path, Function<TableT, NewTableT> table, BiFunction<TableT, NewTableT, Condition> bindCondition) {
			assertContextOpen();
			if (underlyingContext.paths.contains(path)) {
				throw new ReusedPathException("Path already exists: " + path);
			}
			underlyingContext.paths.add(path);
			underlyingContext.subEntityFactory.put(path, (Function<Table, Table>) table);
			underlyingContext.subEntityBindCondition.put(path, (BiFunction<Table, Table, Condition>) bindCondition);
			UnderlyingContext subEntityContext = new UnderlyingContext();
			underlyingContext.subEntityContexts.put(path, subEntityContext);
			return new FactoryBuilderClass<>(subEntityContext, buildContextOpen, false);
		}

		private void assertContextOpen() {
			if (!buildContextOpen.get()) {
				throw new BuildContextClosedException("Cannot add fields after build() has been called");
			}
		}
	}

	private static class UnderlyingContext {
		private final Set<String> paths = new HashSet<>();
		private final Map<String, Function<Table, Field>> fieldFactory = new HashMap<>();
		private final Map<String, Function<Table, Table>> subEntityFactory = new HashMap<>();
		private final Map<String, BiFunction<Table, Table, Condition>> subEntityBindCondition = new HashMap<>();
		private final Map<String, UnderlyingContext> subEntityContexts = new HashMap<>();
	}

	private static class ReusedPathException extends RuntimeException {
		public ReusedPathException(String message) {
			super(message);
		}
	}

	private static class BuildContextClosedException extends RuntimeException {
		public BuildContextClosedException(String message) {
			super(message);
		}
	}

	private static class BindContextFactoryImpl<TableT extends Table> implements BindingContextFactory<TableT> {

		private final UnderlyingContext underlyingContext;

		private BindContextFactoryImpl(UnderlyingContext underlyingContext) {
			this.underlyingContext = underlyingContext;
		}


		@Override
		public PropertyBindingContext startContext(TableT table) {
			return new PropertyBindingContextImpl(
				table,
				null,
				underlyingContext,
				false,
				new AtomicInteger(0)
			);
		}
	}

	private static class PropertyBindingContextImpl implements PropertyBindingSubEntityContext {

		private final Condition bindCondition;
		private final UnderlyingContext underlyingContext;
		private final boolean areWeSubEntity;
		private final Table table;
		private final AtomicInteger aliasGenerator;

		private PropertyBindingContextImpl(Table table, Condition bindCondition, UnderlyingContext underlyingContext, boolean areWeSubEntity, AtomicInteger aliasGenerator) {
			this.table = table;
			this.bindCondition = bindCondition;
			this.underlyingContext = underlyingContext;
			this.areWeSubEntity = areWeSubEntity;
			this.aliasGenerator = aliasGenerator;
		}

		@Override
		public Field getBinding(String path) {
			return underlyingContext.fieldFactory.get(path).apply(table);
		}

		@Override
		public PropertyBindingSubEntityContext<?> getSubEntityContext(String path) {
			UnderlyingContext subEntityContext = underlyingContext.subEntityContexts.get(path);
			if (subEntityContext == null) {
				return null;
			}
			Table subTable = underlyingContext.subEntityFactory.get(path)
				.apply(table)
				.as("alias_" + aliasGenerator.getAndIncrement());
			Condition bindCondition = underlyingContext.subEntityBindCondition.get(path)
				.apply(table, subTable);
			return new PropertyBindingContextImpl(
				subTable,
				bindCondition,
				subEntityContext,
				true,
				aliasGenerator
			);
		}

		@Override
		public Table getTable() {
			if (!areWeSubEntity) {
				throw new BadApiUsageException("Cannot get table from root context");
			}
			return table;
		}

		@Override
		public Condition getTableBindCondition() {
			if (!areWeSubEntity) {
				throw new BadApiUsageException("Cannot get table from root context");
			}
			return bindCondition;
		}
	}

	private static class BadApiUsageException extends RuntimeException {
		public BadApiUsageException(String message) {
			super(message);
		}
	}

}
