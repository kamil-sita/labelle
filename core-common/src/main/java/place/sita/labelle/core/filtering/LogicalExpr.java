package place.sita.labelle.core.filtering;

import place.sita.labelle.core.filtering.logic.OrExpr;

import java.util.List;

public interface LogicalExpr {

    static LogicalExpr all() {
        return new OrExpr(List.of());
    }

}
