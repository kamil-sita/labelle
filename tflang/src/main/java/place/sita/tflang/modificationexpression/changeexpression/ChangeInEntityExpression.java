package place.sita.tflang.modificationexpression.changeexpression;

public non-sealed interface ChangeInEntityExpression extends ChangeExpression {

	String entityName();

	ChangeExpression change();

}
