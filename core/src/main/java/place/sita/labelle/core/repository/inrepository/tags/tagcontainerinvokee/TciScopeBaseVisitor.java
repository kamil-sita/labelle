package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee;

import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.*;

public abstract class TciScopeBaseVisitor<T> {

	public T visit(TciScope scope) {
		return switch (scope) {
			case TciScopeAll tciScopeAll -> visitTciScopeAll(tciScopeAll);
			case TciScopeAnd tciScopeAnd -> visitTciScopeAnd(tciScopeAnd);
			case TciScopeCategoryEqual tciScopeCategoryEqual -> visitTciScopeCategoryEqual(tciScopeCategoryEqual);
			case TciScopeCategoryIn tciScopeCategoryIn -> visitTciScopeCategoryIn(tciScopeCategoryIn);
			case TciScopeCategoryLike tciScopeCategoryLike -> visitTciScopeCategoryLike(tciScopeCategoryLike);
			case TciScopeCategoryTagIn tciScopeCategoryTagIn -> visitTciScopeCategoryTagIn(tciScopeCategoryTagIn);
			case TciScopeNot tciScopeNot -> visitTciScopeNot(tciScopeNot);
			case TciScopeOr tciScopeOr -> visitTciScopeOr(tciScopeOr);
			case TciScopeTagEqual tciScopeTagEqual -> visitTciScopeTagEqual(tciScopeTagEqual);
			case TciScopeTagIn tciScopeTagIn -> visitTciScopeTagIn(tciScopeTagIn);
			case TciScopeTagLike tciScopeTagLike -> visitTciScopeTagLike(tciScopeTagLike);
		};
	}

	protected abstract T visitTciScopeAll(TciScopeAll tciScopeAll);

	protected abstract T visitTciScopeAnd(TciScopeAnd tciScopeAnd);

	protected abstract T visitTciScopeCategoryEqual(TciScopeCategoryEqual tciScopeCategoryEqual);

	protected abstract T visitTciScopeCategoryIn(TciScopeCategoryIn tciScopeCategoryIn);

	protected abstract T visitTciScopeCategoryLike(TciScopeCategoryLike tciScopeCategoryLike);

	protected abstract T visitTciScopeCategoryTagIn(TciScopeCategoryTagIn tciScopeCategoryTagIn);

	protected abstract T visitTciScopeNot(TciScopeNot tciScopeNot);

	protected abstract T visitTciScopeOr(TciScopeOr tciScopeOr);

	protected abstract T visitTciScopeTagEqual(TciScopeTagEqual tciScopeTagEqual);

	protected abstract T visitTciScopeTagIn(TciScopeTagIn tciScopeTagIn);

	protected abstract T visitTciScopeTagLike(TciScopeTagLike tciScopeTagLike);

}
