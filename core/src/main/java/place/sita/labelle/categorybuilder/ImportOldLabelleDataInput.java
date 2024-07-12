package place.sita.labelle.categorybuilder;

import java.util.UUID;

public record ImportOldLabelleDataInput(String categoriesFile, String imageCategoriesFile, UUID repositoryId, String root) {
}
