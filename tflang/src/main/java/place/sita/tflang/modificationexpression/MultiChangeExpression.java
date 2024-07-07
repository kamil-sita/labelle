package place.sita.tflang.modificationexpression;

import java.util.List;

public non-sealed interface MultiChangeExpression extends ChangeExpression {

	List<ChangeExpression> changes();

}
