package place.sita.labelle.gui.local;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxAppReadyForStartEvent extends ApplicationEvent {
    private final ConfigurableApplicationContext context;

    public Stage getStage() {
        return Stage.class.cast(getSource());
    }

    public JavaFxAppReadyForStartEvent(Object source, ConfigurableApplicationContext context) {
        super(source);
        this.context = context;
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }
}
