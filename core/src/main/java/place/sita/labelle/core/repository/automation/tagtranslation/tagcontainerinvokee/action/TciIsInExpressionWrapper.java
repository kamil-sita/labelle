package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.action;

import java.util.List;

public record TciIsInExpressionWrapper(List<TciAction> actions) implements TciAction {
}
