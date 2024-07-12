package place.sita.labelle.categorybuilder.model;

import java.util.List;
import java.util.UUID;

public record Category (
    UUID categoryUuid,
    String name,
    boolean required,
    boolean descriptive,
    List<CategoryValue> categoryValues) {

}
