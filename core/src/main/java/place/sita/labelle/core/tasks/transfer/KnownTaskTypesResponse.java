package place.sita.labelle.core.tasks.transfer;

import java.math.BigDecimal;

public record KnownTaskTypesResponse(
    String code,
    String name,
    BigDecimal delay,
    String sampleValue) {

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
