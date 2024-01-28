package place.sita.labelle.core.filtering.logic;

import place.sita.labelle.core.filtering.LogicalExpr;

import java.util.List;

public record AndExpr(List<LogicalExpr> exprList) implements LogicalExpr {
}
