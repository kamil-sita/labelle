package place.sita.tflang.parsers;

import org.junit.jupiter.api.Test;
import place.sita.tflang.modificationexpression.*;

import static org.assertj.core.api.Assertions.assertThat;

public class StringToChangeExpressionParserTest {

	@Test
	public void shouldParseJustRemoveStatement() {
		// given
		String query = "REMOVE";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(JustRemoveExpression.class);
	}

	@Test
	public void shouldParseSingleRemoveStatement() {
		// given
		String query = "REMOVE (\"value\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(RemoveExpression.class);
		var removeExpression = (RemoveExpression) expression;
		assertThat(removeExpression.remove().dimensionality()).isEqualTo(1);
		assertThat(removeExpression.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression.remove().valueAt(0).stringValue()).isEqualTo("value");
	}

	@Test
	public void shouldParseSingleRemoveBiggerTupleStatement() {
		// given
		String query = "REMOVE (\"value1\", \"value2\", \"value3\", \"value4\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(RemoveExpression.class);
		var removeExpression = (RemoveExpression) expression;
		assertThat(removeExpression.remove().dimensionality()).isEqualTo(4);
		assertThat(removeExpression.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression.remove().valueAt(0).stringValue()).isEqualTo("value1");
		assertThat(removeExpression.remove().valueAt(1).isString()).isTrue();
		assertThat(removeExpression.remove().valueAt(1).stringValue()).isEqualTo("value2");
		assertThat(removeExpression.remove().valueAt(2).isString()).isTrue();
		assertThat(removeExpression.remove().valueAt(2).stringValue()).isEqualTo("value3");
		assertThat(removeExpression.remove().valueAt(3).isString()).isTrue();
		assertThat(removeExpression.remove().valueAt(3).stringValue()).isEqualTo("value4");
	}

	@Test
	public void shouldParseMultipleRemoveStatements() {
		// given
		String query = "REMOVE (\"value1\"), REMOVE (\"value2\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(MultiChangeExpression.class);
		var multiChangeExpression = (MultiChangeExpression) expression;
		assertThat(multiChangeExpression.changes().size()).isEqualTo(2);
		assertThat(multiChangeExpression.changes().get(0)).isInstanceOf(RemoveExpression.class);
		assertThat(multiChangeExpression.changes().get(1)).isInstanceOf(RemoveExpression.class);
		var removeExpression1 = (RemoveExpression) multiChangeExpression.changes().get(0);
		assertThat(removeExpression1.remove().dimensionality()).isEqualTo(1);
		assertThat(removeExpression1.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression1.remove().valueAt(0).stringValue()).isEqualTo("value1");
		var removeExpression2 = (RemoveExpression) multiChangeExpression.changes().get(1);
		assertThat(removeExpression2.remove().dimensionality()).isEqualTo(1);
		assertThat(removeExpression2.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression2.remove().valueAt(0).stringValue()).isEqualTo("value2");
	}

	@Test
	public void shouldRemoveMatchedValues() {
		// given
		String query = "REMOVE (MATCHED, \"value2\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(RemoveExpression.class);
		var removeExpression = (RemoveExpression) expression;
		assertThat(removeExpression.remove().dimensionality()).isEqualTo(2);
		assertThat(removeExpression.remove().valueAt(0).isString()).isFalse();
		assertThat(removeExpression.remove().valueAt(1).isString()).isTrue();
		assertThat(removeExpression.remove().valueAt(1).stringValue()).isEqualTo("value2");
	}

	@Test
	public void shouldRemoveUsingFunction() {
		// given
		String query = "REMOVE USING foobar";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(RemoveUsingFunctionExpression.class);
		var removeExpression = (RemoveUsingFunctionExpression) expression;
		assertThat(removeExpression.functionName()).isEqualTo("foobar");
	}

	@Test
	public void shouldRemoveMany() {
		// given
		String query = "REMOVE (\"value1\"), (\"value2\"), (\"value3\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(MultiChangeExpression.class);
		var multiChangeExpression = (MultiChangeExpression) expression;
		assertThat(multiChangeExpression.changes().size()).isEqualTo(3);

		assertThat(multiChangeExpression.changes().get(0)).isInstanceOf(RemoveExpression.class);
		var removeExpression1 = (RemoveExpression) multiChangeExpression.changes().get(0);
		assertThat(removeExpression1.remove().dimensionality()).isEqualTo(1);
		assertThat(removeExpression1.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression1.remove().valueAt(0).stringValue()).isEqualTo("value1");

		assertThat(multiChangeExpression.changes().get(1)).isInstanceOf(RemoveExpression.class);
		var removeExpression2 = (RemoveExpression) multiChangeExpression.changes().get(1);
		assertThat(removeExpression2.remove().dimensionality()).isEqualTo(1);
		assertThat(removeExpression2.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression2.remove().valueAt(0).stringValue()).isEqualTo("value2");

		assertThat(multiChangeExpression.changes().get(2)).isInstanceOf(RemoveExpression.class);
		var removeExpression3 = (RemoveExpression) multiChangeExpression.changes().get(2);
		assertThat(removeExpression3.remove().dimensionality()).isEqualTo(1);
		assertThat(removeExpression3.remove().valueAt(0).isString()).isTrue();
		assertThat(removeExpression3.remove().valueAt(0).stringValue()).isEqualTo("value3");
	}

	@Test
	public void shouldParseAddExpression() {
		// given
		String query = "ADD (\"value\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(AddExpression.class);
		var addExpression = (AddExpression) expression;
		assertThat(addExpression.add().dimensionality()).isEqualTo(1);
		assertThat(addExpression.add().valueAt(0).isString()).isTrue();
		assertThat(addExpression.add().valueAt(0).stringValue()).isEqualTo("value");
	}

	@Test
	public void shouldParseAddMatchedExpression() {
		// given
		String query = "ADD (MATCHED, \"value\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(AddExpression.class);
		var addExpression = (AddExpression) expression;
		assertThat(addExpression.add().dimensionality()).isEqualTo(2);
		assertThat(addExpression.add().valueAt(0).isString()).isFalse();
		assertThat(addExpression.add().valueAt(1).isString()).isTrue();
		assertThat(addExpression.add().valueAt(1).stringValue()).isEqualTo("value");
	}

	@Test
	public void shouldParseMultipleAddExpressions() {
		// given
		String query = "ADD (\"value1\"), (\"value2\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(MultiChangeExpression.class);
		var multiChangeExpression = (MultiChangeExpression) expression;
		assertThat(multiChangeExpression.changes().size()).isEqualTo(2);
		assertThat(multiChangeExpression.changes().get(0)).isInstanceOf(AddExpression.class);
		assertThat(multiChangeExpression.changes().get(1)).isInstanceOf(AddExpression.class);
		var addExpression1 = (AddExpression) multiChangeExpression.changes().get(0);
		assertThat(addExpression1.add().dimensionality()).isEqualTo(1);
		assertThat(addExpression1.add().valueAt(0).isString()).isTrue();
		assertThat(addExpression1.add().valueAt(0).stringValue()).isEqualTo("value1");
		var addExpression2 = (AddExpression) multiChangeExpression.changes().get(1);
		assertThat(addExpression2.add().dimensionality()).isEqualTo(1);
		assertThat(addExpression2.add().valueAt(0).isString()).isTrue();
		assertThat(addExpression2.add().valueAt(0).stringValue()).isEqualTo("value2");
	}

	@Test
	public void shouldParseAddUsingFunction() {
		// given
		String query = "ADD USING foobar";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(AddUsingFunctionExpression.class);
		var addExpression = (AddUsingFunctionExpression) expression;
		assertThat(addExpression.functionName()).isEqualTo("foobar");
	}

	@Test
	public void shouldParseTransformExpression() {
		// given
		String query = "REPLACE WITH (\"value\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(ModifyExpression.class);
		var modifyExpression = (ModifyExpression) expression;
		assertThat(modifyExpression.modify().dimensionality()).isEqualTo(1);
		assertThat(modifyExpression.modify().valueAt(0).isString()).isTrue();
		assertThat(modifyExpression.modify().valueAt(0).stringValue()).isEqualTo("value");
	}

	@Test
	public void shouldParseTransformMatchedExpression() {
		// given
		String query = "REPLACE WITH (MATCHED, \"value\")";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(ModifyExpression.class);
		var modifyExpression = (ModifyExpression) expression;
		assertThat(modifyExpression.modify().dimensionality()).isEqualTo(2);
		assertThat(modifyExpression.modify().valueAt(0).isString()).isFalse();
		assertThat(modifyExpression.modify().valueAt(1).isString()).isTrue();
		assertThat(modifyExpression.modify().valueAt(1).stringValue()).isEqualTo("value");
	}

	@Test
	public void shouldReplaceWithFunction() {
		// given
		String query = "REPLACE USING foobar";

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(ModifyUsingFunctionExpression.class);
		var modifyExpression = (ModifyUsingFunctionExpression) expression;
		assertThat(modifyExpression.functionName()).isEqualTo("foobar");
	}

	@Test
	public void shouldParseChangeInEntityExpression() {
		// given
		String query = "IN entity DO (ADD (\"value\"))";
		// todo test also IN entity DO (ADD "value") - it probably should be illegal

		// when
		ChangeExpression expression = StringToChangeExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(ChangeInEntityExpression.class);
		var changeInEntityExpression = (ChangeInEntityExpression) expression;
		assertThat(changeInEntityExpression.entityName()).isEqualTo("entity");
		assertThat(changeInEntityExpression.change()).isInstanceOf(AddExpression.class);
		var addExpression = (AddExpression) changeInEntityExpression.change();
		assertThat(addExpression.add().dimensionality()).isEqualTo(1);
		assertThat(addExpression.add().valueAt(0).isString()).isTrue();
		assertThat(addExpression.add().valueAt(0).stringValue()).isEqualTo("value");
	}

}
