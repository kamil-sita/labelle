package place.sita.labelle.categorybuilder.model;

import java.util.List;
import java.util.UUID;

public record ImageCategories(UUID imageCategoryUuid, String path, List<ImageCategoriesValue> imageCategoriesValues) {

}
