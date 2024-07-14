package place.sita.tflang;

import place.sita.tflang.filteringexpression.fillteringexpression.FilteringExpression;
import place.sita.tflang.modificationexpression.changeexpression.ChangeExpression;

public record ConditionalChangeExpression(FilteringExpression filter, ChangeExpression change) {

}
