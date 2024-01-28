package place.sita.labelle.core.persistence;

import org.jooq.Record1;
import org.jooq.Result;
import place.sita.labelle.core.persistence.ex.UnexpectedDatabaseReplyException;

import java.util.List;
import java.util.function.Supplier;

public class JqRepo {

    private JqRepo() {

    }

    public static <T> T fetchOne(List<T> elements) {
        if (elements.size() != 1) {
            throw new UnexpectedDatabaseReplyException("Expected to find one result, got " + elements.size() + " instead.");
        }

        return elements.get(0);
    }

    public static <T> T fetchOne(Supplier<Result<Record1<T>>> partial) {
        var dbCallResult = partial.get();

        if (dbCallResult.size() != 1) {
            throw new UnexpectedDatabaseReplyException("Expected to find one result, got " + dbCallResult.size() + " instead.");
        }

        return dbCallResult.get(0).value1();
    }

    public static void insertOne(Supplier<Integer> partial) {
        var dbCallResult = partial.get();

        if (dbCallResult != 1) {
            throw new UnexpectedDatabaseReplyException("Expected to insert one row, got " + dbCallResult + " instead.");
        }
    }

}
