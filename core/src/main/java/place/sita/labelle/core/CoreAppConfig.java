package place.sita.labelle.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import place.sita.labelle.extensions.ExtensionsConfig;

@Configuration
@ComponentScan
@Import(ExtensionsConfig.class)
public class CoreAppConfig {
}
