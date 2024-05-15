package place.sita.labelle.core.filtering.logic;

import place.sita.labelle.core.filtering.LogicalExpr;

import java.util.List;

public record OrExpr(List<LogicalExpr> exprList) implements LogicalExpr {
}
