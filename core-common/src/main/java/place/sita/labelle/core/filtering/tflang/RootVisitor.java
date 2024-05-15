package place.sita.labelle.core.filtering.tflang;

import org.jooq.Condition;
import org.jooq.Field;
import place.sita.labelle.tflang.TFLangBaseVisitor;
import place.sita.labelle.tflang.TFLangParser;

import java.util.Map;

public class RootVisitor extends TFLangBaseVisitor<Condition> {

    private final Map<String, Field<String>> fieldMapping;

    public RootVisitor(Map<String, Field<String>> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    @Override
    public Condition visitEqComparison(TFLangParser.EqComparisonContext ctx) {
        String fName = ctx.NAME().getText();
        String value = ctx.StringLiteral().getText();
        value = value.substring(1, value.length() - 1); // todo it probably shouldn't look like this
        Field<String> field = fieldMapping.get(fName);
        return field.eq(value);
    }
}
