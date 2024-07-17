package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee;

import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.scope.TciScope;
import place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.action.TciAction;

import java.util.List;

public record TciInstruction(TciScope scope, List<TciAction> actions) {

}
