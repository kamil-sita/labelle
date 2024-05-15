package place.sita.labelle.core.filtering.logic;

import place.sita.labelle.core.filtering.LogicalExpr;

public record NotExpr(LogicalExpr expr) implements LogicalExpr {
}
