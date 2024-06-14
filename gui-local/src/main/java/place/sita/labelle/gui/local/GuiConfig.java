package place.sita.labelle.gui.local;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import place.sita.labelle.core.CoreAppConfig;
import place.sita.modulefx.SpringChildrenFactory;

@Import({CoreAppConfig.class, SpringChildrenFactory.class})
@EnableScheduling
@SpringBootApplication
public class GuiConfig {
}
