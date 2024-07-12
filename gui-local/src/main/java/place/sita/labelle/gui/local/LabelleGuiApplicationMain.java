package place.sita.labelle.gui.local;

import javafx.application.Application;
import org.springframework.context.annotation.Import;

import java.awt.*;

@Import(GuiConfig.class)
public class LabelleGuiApplicationMain {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        SplashScreen.getSplashScreen();

        Application.launch(SpringJavaFxApplicationConfig.class, args);
    }

}
