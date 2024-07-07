package place.sita.tflang.parsers;

import org.junit.Test;
import place.sita.tflang.SemanticException;
import place.sita.tflang.filteringexpression.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StringToTFlangFilteringExpressionParserTest {

	@Test
	public void shouldParseSimpleExpression() {
		// given
		String query = "test = \"123\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) expression;
		assertThat(equalExpression.key()).isEqualTo("test");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldParseSingleElementTuples() {
		// given
		String query = "(test1) = (\"123\")";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) expression;
		assertThat(equalExpression.key()).isEqualTo("test1");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldThrowOnMismatchedTuple() {
		// given
		String query = "(test1, test2) = (\"123\")";

		// when / then
		assertThatThrownBy(() -> StringToFilteringExpressionParser.parse(query))
				.isInstanceOf(SemanticException.class);
	}

	@Test
	public void shouldParseTupleMatchExpression() {
		// given
		String query = "(test1, test2) = (\"123\", \"456\")";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(AndExpression.class);
		AndExpression andExpression = (AndExpression) expression;
		assertThat(andExpression.expressions()).hasSize(2);
		assertThat(andExpression.expressions().get(0)).isInstanceOf(EqualExpression.class);
		EqualExpression first = (EqualExpression) andExpression.expressions().get(0);
		assertThat(first.key()).isEqualTo("test1");
		assertThat(first.value()).isEqualTo("123");
		assertThat(andExpression.expressions().get(1)).isInstanceOf(EqualExpression.class);
		EqualExpression second = (EqualExpression) andExpression.expressions().get(1);
		assertThat(second.key()).isEqualTo("test2");
		assertThat(second.value()).isEqualTo("456");
	}

	@Test
	public void shouldParseMatchAnyExpression() {
		// given
		String query = "ANY";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isSameAs(FilteringExpression.MATCH_EVERYTHING);
		assertThat(expression).isInstanceOf(EverythingExpression.class);
	}

	@Test
	public void shouldParseOrExpressions() {
		// given
		String query = "test1 = \"123\" OR test2 = \"456\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(OrExpression.class);
		OrExpression orExpression = (OrExpression) expression;
		assertThat(orExpression.expressions()).hasSize(2);
		assertThat(orExpression.expressions().get(0)).isInstanceOf(EqualExpression.class);
		EqualExpression first = (EqualExpression) orExpression.expressions().get(0);
		assertThat(first.key()).isEqualTo("test1");
		assertThat(first.value()).isEqualTo("123");
		assertThat(orExpression.expressions().get(1)).isInstanceOf(EqualExpression.class);
		EqualExpression second = (EqualExpression) orExpression.expressions().get(1);
		assertThat(second.key()).isEqualTo("test2");
		assertThat(second.value()).isEqualTo("456");
	}

	@Test
	public void shouldParseAndExpressions() {
		// given
		String query = "test1 = \"123\" AND test2 = \"456\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(AndExpression.class);
		AndExpression andExpression = (AndExpression) expression;
		assertThat(andExpression.expressions()).hasSize(2);
		assertThat(andExpression.expressions().get(0)).isInstanceOf(EqualExpression.class);
		EqualExpression first = (EqualExpression) andExpression.expressions().get(0);
		assertThat(first.key()).isEqualTo("test1");
		assertThat(first.value()).isEqualTo("123");
		assertThat(andExpression.expressions().get(1)).isInstanceOf(EqualExpression.class);
		EqualExpression second = (EqualExpression) andExpression.expressions().get(1);
		assertThat(second.key()).isEqualTo("test2");
		assertThat(second.value()).isEqualTo("456");
	}

	@Test
	public void shouldParseMultipleOrExpressions() {
		// given
		String query = "test1 = \"123\" OR test2 = \"456\" OR test3 = \"789\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(OrExpression.class);
		OrExpression orExpression = (OrExpression) expression;
		assertThat(orExpression.expressions()).hasSize(3);
		assertThat(orExpression.expressions().get(0)).isInstanceOf(EqualExpression.class);
		EqualExpression first = (EqualExpression) orExpression.expressions().get(0);
		assertThat(first.key()).isEqualTo("test1");
		assertThat(first.value()).isEqualTo("123");
		assertThat(orExpression.expressions().get(1)).isInstanceOf(EqualExpression.class);
		EqualExpression second = (EqualExpression) orExpression.expressions().get(1);
		assertThat(second.key()).isEqualTo("test2");
		assertThat(second.value()).isEqualTo("456");
		assertThat(orExpression.expressions().get(2)).isInstanceOf(EqualExpression.class);
		EqualExpression third = (EqualExpression) orExpression.expressions().get(2);
		assertThat(third.key()).isEqualTo("test3");
		assertThat(third.value()).isEqualTo("789");
	}

	@Test
	public void shouldParseMultipleAndExpressions() {
		// given
		String query = "test1 = \"123\" AND test2 = \"456\" AND test3 = \"789\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(AndExpression.class);
		AndExpression andExpression = (AndExpression) expression;
		assertThat(andExpression.expressions()).hasSize(3);
		assertThat(andExpression.expressions().get(0)).isInstanceOf(EqualExpression.class);
		EqualExpression first = (EqualExpression) andExpression.expressions().get(0);
		assertThat(first.key()).isEqualTo("test1");
		assertThat(first.value()).isEqualTo("123");
		assertThat(andExpression.expressions().get(1)).isInstanceOf(EqualExpression.class);
		EqualExpression second = (EqualExpression) andExpression.expressions().get(1);
		assertThat(second.key()).isEqualTo("test2");
		assertThat(second.value()).isEqualTo("456");
		assertThat(andExpression.expressions().get(2)).isInstanceOf(EqualExpression.class);
		EqualExpression third = (EqualExpression) andExpression.expressions().get(2);
		assertThat(third.key()).isEqualTo("test3");
		assertThat(third.value()).isEqualTo("789");
	}

	@Test
	public void shouldMatchNotExpression() {
		// given
		String query = "NOT test = \"123\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(NotExpression.class);
		NotExpression notExpression = (NotExpression) expression;
		assertThat(notExpression.expression()).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) notExpression.expression();
		assertThat(equalExpression.key()).isEqualTo("test");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldMatchDoubleNotExpression() {
		// given
		String query = "NOT NOT test = \"123\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) expression;
		assertThat(equalExpression.key()).isEqualTo("test");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldShortCircuitAndExpressions() {
		// given
		String query = "test1 = \"123\" AND ANY";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) expression;
		assertThat(equalExpression.key()).isEqualTo("test1");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldShortCircuitOrExpressions() {
		// given
		String query = "test1 = \"123\" OR ANY";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isSameAs(FilteringExpression.MATCH_EVERYTHING);
		assertThat(expression).isInstanceOf(EverythingExpression.class);
	}

	@Test
	public void shouldParseSingleElementInExpression() {
		// given
		String query = "test in (\"123\")";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) expression;
		assertThat(equalExpression.key()).isEqualTo("test");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldParseMultipleElementInExpression() {
		// given
		String query = "test in (\"123\", \"456\")";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(InExpression.class);
		InExpression inExpression = (InExpression) expression;
		assertThat(inExpression.key()).isEqualTo("test");
		assertThat(inExpression.values()).containsExactly("123", "456");
	}

	@Test
	public void shouldMatchLikeExpressions() {
		// given
		String query = "test like \"123*\"";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(LikeExpression.class);
		LikeExpression likeExpression = (LikeExpression) expression;
		assertThat(likeExpression.key()).isEqualTo("test");
		assertThat(likeExpression.value()).isEqualTo("123*");
	}

	@Test
	public void shouldParseSingleElementTupleInExpression() {
		// given
		String query = "(test1) in ((\"123\"))";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(EqualExpression.class);
		EqualExpression equalExpression = (EqualExpression) expression;
		assertThat(equalExpression.key()).isEqualTo("test1");
		assertThat(equalExpression.value()).isEqualTo("123");
	}

	@Test
	public void shouldParseSingleElementTupleInMultipleExpression() {
		// given
		String query = "(test1) in ((\"123\"), (\"456\"))";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(OrExpression.class);
		OrExpression orExpression = (OrExpression) expression;
		assertThat(orExpression.expressions()).hasSize(2);
		assertThat(orExpression.expressions().get(0)).isInstanceOf(EqualExpression.class);
		EqualExpression first = (EqualExpression) orExpression.expressions().get(0);
		assertThat(first.key()).isEqualTo("test1");
		assertThat(first.value()).isEqualTo("123");
		assertThat(orExpression.expressions().get(1)).isInstanceOf(EqualExpression.class);
		EqualExpression second = (EqualExpression) orExpression.expressions().get(1);
		assertThat(second.key()).isEqualTo("test1");
		assertThat(second.value()).isEqualTo("456");
	}

	@Test
	public void shouldParseMultipleElementTupleInExpression() {
		// given
		String query = "(test1, test2) in ((\"123\", \"456\"))";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(InTupleExpression.class);
		InTupleExpression inTupleExpression = (InTupleExpression) expression;
		assertThat(inTupleExpression.keys()).containsExactly("test1", "test2");
		assertThat(inTupleExpression.values()).hasSize(1);
		assertThat(inTupleExpression.values().get(0)).containsExactly("123", "456");
	}

	@Test
	public void shouldParseMultipleElementTupleInMultipleExpression() {
		// given
		String query = "(test1, test2) in ((\"123\", \"456\"), (\"789\", \"012\"))";

		// when
		FilteringExpression expression = StringToFilteringExpressionParser.parse(query);

		// then
		assertThat(expression).isInstanceOf(InTupleExpression.class);
		InTupleExpression inTupleExpression = (InTupleExpression) expression;
		assertThat(inTupleExpression.keys()).containsExactly("test1", "test2");
		assertThat(inTupleExpression.values()).hasSize(2);
		assertThat(inTupleExpression.values().get(0)).containsExactly("123", "456");
		assertThat(inTupleExpression.values().get(1)).containsExactly("789", "012");
	}

	@Test
	public void shouldThrowOnMismatchedTupleInExpression() {
		// given
		String query = "(test1, test2) in ((\"123\"))";

		// when / then
		assertThatThrownBy(() -> StringToFilteringExpressionParser.parse(query))
				.isInstanceOf(SemanticException.class);
	}

}
