package place.sita.labelle.gui.local;

import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class JavaFxAppReadyForStartEventListener implements ApplicationListener<JavaFxAppReadyForStartEvent> {

	private final StageConfiguration stageConfiguration;

	public JavaFxAppReadyForStartEventListener(StageConfiguration stageConfiguration) {
		this.stageConfiguration = stageConfiguration;
	}

	@Override
	public void onApplicationEvent(JavaFxAppReadyForStartEvent event) {
		SplashScreen splashScreen = SplashScreen.getSplashScreen();
		if (splashScreen != null) {
			splashScreen.close();
		}

		Stage stage = event.getStage();
		stageConfiguration.configureStage(stage);
	}
}
