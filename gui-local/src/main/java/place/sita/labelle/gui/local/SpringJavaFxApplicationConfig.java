package place.sita.labelle.gui.local;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import org.springframework.context.support.GenericApplicationContext;

@Component
public class SpringJavaFxApplicationConfig extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        ApplicationContextInitializer<GenericApplicationContext> initializer = new ApplicationContextInitializer<GenericApplicationContext>() {
            @Override
            public void initialize(GenericApplicationContext genericApplicationContext) {
                genericApplicationContext.registerBean(Application.class, () -> SpringJavaFxApplicationConfig.this);
                genericApplicationContext.registerBean(Parameters.class, () -> getParameters());
                genericApplicationContext.registerBean(HostServices.class, () -> getHostServices());
            }
        };

        this.context = new SpringApplicationBuilder().sources(LabelleGuiApplicationMain.class)
            .initializers(initializer)
            .build().run(getParameters().getRaw().toArray(new String[0]));
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        context.publishEvent(new JavaFxAppReadyForStartEvent(primaryStage, context));
    }

}
