package place.sita.magicscheduler.transfer;

import java.math.BigDecimal;

public record KnownTaskTypesResponse(
    String code,
    String name,
    BigDecimal delay,
    String sampleValue,
    boolean isHistoric) {

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
