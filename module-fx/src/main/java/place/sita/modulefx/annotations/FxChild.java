package place.sita.modulefx.annotations;

import java.lang.annotation.*;

/**
 * This field will have injected the controller of the child node.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FxChild {

	/**
	 * The name of the field in the controller that refers to this child node.
	 * This node will be injected into the field.
	 */
	String patchNode();
}
