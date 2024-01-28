package place.sita.modulefx.annotations;

import java.lang.annotation.*;

/**
 * Methods annotates with this annotation will be called after JavaFX setup is complete. In particular, this
 * will might happen after bean initialization and JavaFX setup is done, meaning that a method annotated with this
 * annotation could connect beans to JavaFX nodes.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostFxConstruct {

}
