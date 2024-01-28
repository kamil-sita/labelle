package place.sita.modulefx.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FxTab { // todo partially replace with FxNode?
    String resourceFile();
    int order();
    String tabName();
}
