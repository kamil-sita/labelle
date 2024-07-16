package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope;

public sealed interface TciTagScope extends TciScope permits
	TciScopeCategoryEqual,
	TciScopeCategoryIn,
	TciScopeCategoryLike,
	TciScopeCategoryTagIn,
	TciScopeTagEqual,
	TciScopeTagIn,
	TciScopeTagLike {
}
