package place.sita.magicscheduler.tasktype.annotationdef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter which should be replaced by an instance of {@link place.sita.magicscheduler.scheduler.BaseTaskSandbox},
 * see {@link MsTask}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MsSandbox {

}
