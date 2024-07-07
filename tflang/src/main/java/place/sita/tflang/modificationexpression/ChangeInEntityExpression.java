package place.sita.tflang.modificationexpression;

public non-sealed interface ChangeInEntityExpression extends ChangeExpression {

	String entityName();

	ChangeExpression change();

}
