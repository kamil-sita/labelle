package place.sita.labelle.categorybuilder.model;

import java.util.UUID;

public record ImageCategoriesValue(
    UUID imageCategoriesValueUuid,
    UUID categoryUuid,
    UUID categoryValueUuid,
    String descriptiveModifier) {
}
