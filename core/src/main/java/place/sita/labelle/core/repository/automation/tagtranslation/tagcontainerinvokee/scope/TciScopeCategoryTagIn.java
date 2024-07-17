package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.scope;

import place.sita.labelle.core.repository.inrepository.tags.Tag;

import java.util.List;

public record TciScopeCategoryTagIn(List<Tag> in) implements TciTagScope {
}
