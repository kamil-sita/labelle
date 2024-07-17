package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.scope;

public sealed interface TciAnyScope extends TciScope permits
		TciScopeAll,
		TciScopeAnd,
		TciScopeNot,
		TciScopeOr {
}
