package place.sita.labelle.categorybuilder.model;

import java.util.UUID;

public record CategoryValue(
    UUID categoryValueUuid,
    String displayedValue,
    String taughtValue,
    String counterTaughtValue) {
}
