package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory;

import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.TciScopeBaseVisitor;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TciScopeToTagFilterConverter extends TciScopeBaseVisitor<TagFiltering> {
	@Override
	protected TagFiltering visitExists(TciExists tciExists) {
		TagFiltering tagFiltering = visit(tciExists.expression());
		Scope scope = resolveScope(tagFiltering);
		if (scope != Scope.TAG) {
			throw new UnexpectedExpressionException();
		}
		TagFilter tagFilter = (TagFilter) tagFiltering;
		return (ContainerFiltering) tags -> {
			for (Tag tag : tags) {
				if (tagFilter.filter(tag)) {
					return true;
				}
			}
			return false;
		};
	}

	@Override
	protected TagFilter visitTciScopeAll(TciScopeAll tciScopeAll) {
		return tag -> true;
	}

	@Override
	protected TagFiltering visitTciScopeAnd(TciScopeAnd tciScopeAnd) {
		List<TagFiltering> filters = new ArrayList<>();
		for (TciScope scope : tciScopeAnd.and()) {
			TagFiltering filter = visit(scope);
			filters.add(filter);
		}
		Scope scope = resolveScope(filters);
		return switch (scope) {
			case CONTAINER -> {
				List<ContainerFiltering> actualFiltering = (List<ContainerFiltering>) (List<?>) filters;
				yield (ContainerFiltering) tags -> {
					for (ContainerFiltering filter : actualFiltering) {
						if (!filter.filter(tags)) {
							return false;
						}
					}
					return true;
				};
			}
			case TAG -> {
				List<TagFilter> actualFiltering = (List<TagFilter>) (List<?>) filters;
				yield (TagFilter) tag -> {
					for (TagFilter filter : actualFiltering) {
						if (!filter.filter(tag)) {
							return false;
						}
					}
					return true;
				};
			}
			case MIXED -> throw new UnexpectedExpressionException();
		};
	}

	@Override
	protected TagFilter visitTciScopeCategoryEqual(TciScopeCategoryEqual tciScopeCategoryEqual) {
		return tag -> tag.category().equals(tciScopeCategoryEqual.category());
	}

	@Override
	protected TagFilter visitTciScopeCategoryIn(TciScopeCategoryIn tciScopeCategoryIn) {
		Set<String> in = new HashSet<>(tciScopeCategoryIn.in());
		return tag -> in.contains(tag.category());
	}

	@Override
	protected TagFilter visitTciScopeCategoryLike(TciScopeCategoryLike tciScopeCategoryLike) {
		Pattern pattern = Pattern.compile(tciScopeCategoryLike.like());
		return tag -> pattern.matcher(tag.category()).matches();
	}

	@Override
	protected TagFilter visitTciScopeCategoryTagIn(TciScopeCategoryTagIn tciScopeCategoryTagIn) {
		Set<Tag> in = new HashSet<>(tciScopeCategoryTagIn.in());
		return tag -> in.contains(tag);
	}

	@Override
	protected TagFiltering visitTciScopeNot(TciScopeNot tciScopeNot) {
		TagFiltering filter = visit(tciScopeNot.not());
		Scope scope = resolveScope(filter);
		return switch (scope) {
			case CONTAINER -> {
				ContainerFiltering actualFilter = (ContainerFiltering) filter;
				yield (ContainerFiltering) tags -> !actualFilter.filter(tags);
			}
			case TAG -> {
				TagFilter actualFilter = (TagFilter) filter;
				yield (TagFilter) tag -> !actualFilter.filter(tag);
			}
			case MIXED -> throw new UnexpectedExpressionException();
		};
	}

	@Override
	protected TagFiltering visitTciScopeOr(TciScopeOr tciScopeOr) {
		List<TagFiltering> filters = new ArrayList<>();
		for (TciScope scope : tciScopeOr.or()) {
			TagFiltering filter = visit(scope);
			filters.add(filter);
		}
		Scope scope = resolveScope(filters);
		return switch (scope) {
			case CONTAINER -> {
				List<ContainerFiltering> actualFiltering = (List<ContainerFiltering>) (List<?>) filters;
				yield (ContainerFiltering) tags -> {
					for (ContainerFiltering filter : actualFiltering) {
						if (filter.filter(tags)) {
							return true;
						}
					}
					return false;
				};
			}
			case TAG -> {
				List<TagFilter> actualFiltering = (List<TagFilter>) (List<?>) filters;
				yield (TagFilter) tag -> {
					for (TagFilter filter : actualFiltering) {
						if (filter.filter(tag)) {
							return true;
						}
					}
					return false;
				};
			}
			case MIXED -> throw new UnexpectedExpressionException();
		};
	}

	@Override
	protected TagFilter visitTciScopeTagEqual(TciScopeTagEqual tciScopeTagEqual) {
		return tag -> tag.tag().equals(tciScopeTagEqual.tag());
	}

	@Override
	protected TagFilter visitTciScopeTagIn(TciScopeTagIn tciScopeTagIn) {
		Set<String> in = new HashSet<>(tciScopeTagIn.in());
		return tag -> in.contains(tag.tag());
	}

	@Override
	protected TagFilter visitTciScopeTagLike(TciScopeTagLike tciScopeTagLike) {
		Pattern pattern = Pattern.compile(tciScopeTagLike.like());
		return tag -> pattern.matcher(tag.tag()).matches();
	}

	private static Scope resolveScope(TagFiltering tagFiltering) {
		return switch (tagFiltering) {
			case ContainerFiltering containerFiltering -> Scope.CONTAINER;
			case TagFilter tagFilter -> Scope.TAG;
		};
	}

	private static Scope resolveScope(Collection<TagFiltering> tagFilters) {
		Scope scope = null;
		for (TagFiltering tagFilter : tagFilters) {
			Scope myScope = resolveScope(tagFilter);
			if (scope == null) {
				scope = myScope;
			} else {
				if (scope != myScope) {
					scope = Scope.MIXED;
				}
			}
		}
		return scope;
	}

	private enum Scope {
		CONTAINER,
		TAG,
		MIXED,
		;
	}
}
