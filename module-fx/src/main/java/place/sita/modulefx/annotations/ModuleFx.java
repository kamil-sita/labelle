package place.sita.modulefx.annotations;

import java.lang.annotation.*;

/**
 * Generic annotation to autowire a ModuleFx utility.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModuleFx {
}
