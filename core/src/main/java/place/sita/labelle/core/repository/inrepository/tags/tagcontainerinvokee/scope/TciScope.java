package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope;

public sealed interface TciScope permits
	TciScopeAnd,
	TciScopeCategoryEqual,
	TciScopeCategoryIn,
	TciScopeCategoryLike,
	TciScopeCategoryTagEqual,
	TciScopeCategoryTagIn,
	TciScopeNot,
	TciScopeOr,
	TciScopeTagEqual,
	TciScopeTagIn,
	TciScopeTagLike,
	TciScopeAll {
}
