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
}
