package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope;

public sealed interface TciAnyScope extends TciScope permits
		TciScopeAll,
		TciScopeAnd,
		TciScopeNot,
		TciScopeOr {
}
