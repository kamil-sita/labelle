package place.sita.magicscheduler.tasktype.annotationdef;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Definition of a task execution logic for {@link place.sita.magicscheduler.scheduler.MagicScheduler}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MsTask {
	String code();
	String name();
}
