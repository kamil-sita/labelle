package place.sita.modulefx.annotations;


import java.lang.annotation.*;

/**
 * This field will have injected the controller of the parent node.
 */
 @Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parent {

}
