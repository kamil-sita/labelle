package place.sita.labelle.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {

    private ExceptionUtil() {
        
    }

    public static String exceptionToString(Throwable e) {
        String exception;
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        exception = stringWriter.toString();
        return exception;
    }
}
