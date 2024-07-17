package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.scope;

import java.util.List;

public record TciScopeOr(List<TciScope> or) implements TciAnyScope {
}
