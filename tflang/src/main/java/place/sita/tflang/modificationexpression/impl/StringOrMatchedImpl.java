package place.sita.tflang.modificationexpression.impl;

import place.sita.tflang.modificationexpression.StringOrMatched;

public record StringOrMatchedImpl(boolean isString, String stringValue) implements StringOrMatched {

}
