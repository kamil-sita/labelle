package place.sita.magicscheduler;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import place.sita.labelle.core.CoreCommonAppConfig;

@ComponentScan
@EnableAutoConfiguration
@Import(CoreCommonAppConfig.class)
public class MagicSchedulerConfig {
}
