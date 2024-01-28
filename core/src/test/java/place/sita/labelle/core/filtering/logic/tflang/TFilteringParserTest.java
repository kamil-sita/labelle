package place.sita.labelle.core.filtering.logic.tflang;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import place.sita.labelle.core.filtering.tflang.TFilteringParser;

public class TFilteringParserTest {

    @Test // todo move this class
    @Disabled
    public void shouldNotCrash() {
        String query = """
        abc = "abc"
        """;

        TFilteringParser.condition(query);
    }

}
