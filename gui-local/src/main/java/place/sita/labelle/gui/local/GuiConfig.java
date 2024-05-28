package place.sita.labelle.gui.local;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import place.sita.labelle.core.CoreAppConfig;

@Import(CoreAppConfig.class)
@EnableScheduling
@SpringBootApplication
public class GuiConfig {
}
