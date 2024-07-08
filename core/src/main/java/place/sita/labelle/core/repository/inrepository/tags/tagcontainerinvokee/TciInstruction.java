package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee;

import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.action.TciAction;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.TciScope;

public record TciInstruction(TciScope scope, TciAction action) {

}
