package place.sita.modulefx.annotations;

import place.sita.modulefx.LoadMode;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FxTab { // todo partially replace with FxNode?
    String resourceFile();
    int order();
    String tabName();
    LoadMode loadMode() default LoadMode.ONLY_WHEN_NEEDED;
}
