package place.sita.modulefx.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FxInjectTabs {
	Class<?> value();
}
