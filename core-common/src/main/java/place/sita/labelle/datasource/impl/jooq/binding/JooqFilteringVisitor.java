package place.sita.labelle.datasource.impl.jooq.binding;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import place.sita.labelle.datasource.impl.jooq.binding.binding.PropertyBindingContext;
import place.sita.tflang.filteringexpression.FilteringExpressionBaseVisitor;
import place.sita.tflang.filteringexpression.fillteringexpression.*;

import java.util.ArrayList;
import java.util.List;

import static place.sita.labelle.datasource.impl.jooq.binding.TypeUtil.withCommonType;
import static place.sita.labelle.datasource.impl.jooq.binding.TypeUtil.withCommonTypes;

public class JooqFilteringVisitor extends FilteringExpressionBaseVisitor<Condition> {

	private final PropertyBindingContext bindings;

	public JooqFilteringVisitor(PropertyBindingContext bindings) {
		this.bindings = bindings;
	}

	@Override
	protected Condition visitInSubEntity(InSubEntityExpression inSubEntityExpression) {
		var subEntityContext = bindings.getSubEntityContext(inSubEntityExpression.subEntity());
		if (subEntityContext == null) {
			throw new UnknownPropertyException("Unknown sub-entity: " + inSubEntityExpression.subEntity());
		}
		Table table = subEntityContext.getTable();

		Condition subEntityCondition = new JooqFilteringVisitor(
			subEntityContext
		).visit(inSubEntityExpression.expression());

		Condition join = subEntityContext.getTableBindCondition();

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
		Field<?> binding = bindings.getBinding(property);
		if (binding == null) {
			throw new UnknownPropertyException("Unknown property: " + property);
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
		Field<?> binding = bindings.getBinding(property);
		if (binding == null) {
			throw new UnknownPropertyException("Unknown property: " + property);
		}
		return withCommonTypes(binding, (List<Object>) (List) inExpression.values(), (field, value) -> field.in(value));
	}

	@Override
	protected Condition visitInTuple(InTupleExpression inTupleExpression) {
		List<String> properties = inTupleExpression.keys();
		List<Field> resolvedBindings = new ArrayList<>();
		List<Condition> ors = new ArrayList<>();
		for (String property : properties) {
			Field binding = bindings.getBinding(property);
			if (binding == null) {
				throw new UnknownPropertyException("Unknown property: " + property);
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
		Field binding = bindings.getBinding(property);
		if (binding == null) {
			throw new UnknownPropertyException("Unknown property: " + property);
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
