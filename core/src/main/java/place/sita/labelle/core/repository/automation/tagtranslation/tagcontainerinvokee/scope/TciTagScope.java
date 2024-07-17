package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.scope;

public sealed interface TciTagScope extends TciScope permits
	TciScopeCategoryEqual,
	TciScopeCategoryIn,
	TciScopeCategoryLike,
	TciScopeCategoryTagIn,
	TciScopeTagEqual,
	TciScopeTagIn,
	TciScopeTagLike {
}
