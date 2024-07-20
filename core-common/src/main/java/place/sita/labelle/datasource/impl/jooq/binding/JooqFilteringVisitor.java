package place.sita.labelle.datasource.impl.jooq.binding;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import place.sita.tflang.filteringexpression.FilteringExpressionBaseVisitor;
import place.sita.tflang.filteringexpression.fillteringexpression.*;

import java.util.ArrayList;
import java.util.List;

import static place.sita.labelle.datasource.impl.jooq.binding.TypeUtil.withCommonType;
import static place.sita.labelle.datasource.impl.jooq.binding.TypeUtil.withCommonTypes;

public class JooqFilteringVisitor extends FilteringExpressionBaseVisitor<Condition> {

	private final List<String> path;
	private final JooqPropertyBindings bindings;

	public JooqFilteringVisitor(List<String> path, JooqPropertyBindings bindings) {
		this.path = path;
		this.bindings = bindings;
	}

	@Override
	protected Condition visitInSubEntity(InSubEntityExpression inSubEntityExpression) {
		Table table = bindings.getTable(LogicalPath.path(path, inSubEntityExpression.subEntity()));
		if (table == null) {
			throw new UnknownPropertyException("Unknown sub-entity: " + inSubEntityExpression.subEntity());
		}

		List<String> newPath = new ArrayList<>(path);
		newPath.add(inSubEntityExpression.subEntity());

		Condition subEntityCondition = new JooqFilteringVisitor(
			newPath,
			bindings
		).visit(inSubEntityExpression.expression());

		Condition join = bindings.getTableJoin(LogicalPath.path(path, inSubEntityExpression.subEntity()));

		if (join != null) {
			subEntityCondition = DSL.and(subEntityCondition, join);
		}

		return DSL.exists(
			DSL.selectOne()
				.from(table)
				.where(subEntityCondition)
		);
	}

	@Override
	protected Condition visitMatchEverything() {
		return DSL.noCondition();
	}

	@Override
	protected Condition visitMatchNothing() {
		return DSL.not(DSL.noCondition());
	}

	@Override
	protected Condition visitEqual(EqualExpression equalExpression) {
		String property = equalExpression.key();
		LogicalPath path = LogicalPath.path(this.path, property);
		Field<?> binding = bindings.getBinding(path);
		if (binding == null) {
			throw new UnknownPropertyException("Unknown property: " + path);
		}
		return withCommonType(binding, equalExpression.value(), (field, value) -> field.eq(value));
	}

	@Override
	protected Condition visitEverything(EverythingExpression everythingExpression) {
		return DSL.noCondition();
	}

	@Override
	protected Condition visitIn(InExpression inExpression) {
		String property = inExpression.key();
		LogicalPath path = LogicalPath.path(this.path, property);
		Field<?> binding = bindings.getBinding(path);
		if (binding == null) {
			throw new UnknownPropertyException("Unknown property: " + path);
		}
		return withCommonTypes(binding, (List<Object>) (List) inExpression.values(), (field, value) -> field.in(value));
	}

	@Override
	protected Condition visitInTuple(InTupleExpression inTupleExpression) {
		List<String> properties = inTupleExpression.keys();
		List<Field> resolvedBindings = new ArrayList<>();
		List<Condition> ors = new ArrayList<>();
		for (String property : properties) {
			LogicalPath path = LogicalPath.path(this.path, property);
			Field binding = bindings.getBinding(path);
			if (binding == null) {
				throw new UnknownPropertyException("Unknown property: " + path);
			}
			resolvedBindings.add(binding);
		}
		for (List<String> tuple : inTupleExpression.values()) {
			List<Condition> ands = new ArrayList<>();
			for (int i = 0; i < tuple.size(); i++) {
				Field<?> binding = resolvedBindings.get(i);
				String value = tuple.get(i);
				ands.add(withCommonType(binding, value, (field, v) -> field.eq(v)));
			}
			ors.add(DSL.and(ands));
		}
		return DSL.or(ors);
	}

	@Override
	protected Condition visitLike(LikeExpression likeExpression) {
		String property = likeExpression.key();
		LogicalPath path = LogicalPath.path(this.path, property);
		Field binding = bindings.getBinding(path);
		if (binding == null) {
			throw new UnknownPropertyException("Unknown property: " + path);
		}
		return binding.like(likeExpression.value());
	}

	@Override
	protected Condition visitAnd(AndExpression andExpression) {
		List<Condition> conditions = new ArrayList<>();
		for (FilteringExpression expression : andExpression.expressions()) {
			conditions.add(visit(expression));
		}
		return DSL.and(conditions);
	}

	@Override
	protected Condition visitNot(NotExpression notExpression) {
		Condition condition = visit(notExpression.expression());
		return DSL.not(condition);
	}

	@Override
	protected Condition visitOr(OrExpression orExpression) {
		List<Condition> conditions = new ArrayList<>();
		for (FilteringExpression expression : orExpression.expressions()) {
			conditions.add(visit(expression));
		}
		return DSL.or(conditions);
	}
}
