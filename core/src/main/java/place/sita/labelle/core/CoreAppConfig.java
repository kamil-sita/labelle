package place.sita.labelle.core;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import place.sita.labelle.categorybuilder.CategoryBuilderConfig;
import place.sita.labelle.extensions.ExtensionsConfig;
import place.sita.magicscheduler.MagicSchedulerConfig;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@Import({ExtensionsConfig.class, MagicSchedulerConfig.class, CoreCommonAppConfig.class, CategoryBuilderConfig.class})
public class CoreAppConfig {
}
