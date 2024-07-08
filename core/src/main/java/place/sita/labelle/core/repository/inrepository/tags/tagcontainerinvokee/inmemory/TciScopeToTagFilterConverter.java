package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.inmemory;

import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.TciScopeBaseVisitor;
import place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee.scope.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class TciScopeToTagFilterConverter extends TciScopeBaseVisitor<TagFilter> {
	@Override
	protected TagFilter visitTciScopeAll(TciScopeAll tciScopeAll) {
		return tag -> true;
	}

	@Override
	protected TagFilter visitTciScopeAnd(TciScopeAnd tciScopeAnd) {
		List<TagFilter> filters = new ArrayList<>();
		for (TciScope scope : tciScopeAnd.and()) {
			TagFilter filter = visit(scope);
			filters.add(filter);
		}
		return tag -> {
			for (TagFilter filter : filters) {
				if (!filter.filter(tag)) {
					return false;
				}
			}
			return true;
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
	protected TagFilter visitTciScopeNot(TciScopeNot tciScopeNot) {
		TagFilter filter = visit(tciScopeNot.not());
		return tag -> !filter.filter(tag);
	}

	@Override
	protected TagFilter visitTciScopeOr(TciScopeOr tciScopeOr) {
		List<TagFilter> filters = new ArrayList<>();
		for (TciScope scope : tciScopeOr.or()) {
			TagFilter filter = visit(scope);
			filters.add(filter);
		}
		return tag -> {
			for (TagFilter filter : filters) {
				if (filter.filter(tag)) {
					return true;
				}
			}
			return false;
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
}
