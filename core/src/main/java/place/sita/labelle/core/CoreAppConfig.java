package place.sita.labelle.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import place.sita.labelle.extensions.ExtensionsConfig;
import place.sita.magicscheduler.MagicSchedulerConfig;

@Configuration
@ComponentScan
@Import({ExtensionsConfig.class, MagicSchedulerConfig.class, CoreCommonAppConfig.class})
public class CoreAppConfig {
}
