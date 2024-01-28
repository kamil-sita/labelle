package place.sita.labelle.gui.local.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FxSceneBuilder {
    private static final Logger log = LoggerFactory.getLogger(FxSceneBuilder.class);

    public static Node setupFxView(Object controller, String resource) {
        Class<?> clazz = controller.getClass();
        log.debug("[{}], Setting up FX view", clazz);
        log.debug("[{}] Creating parent loader", clazz);
        FXMLLoader loader = new FXMLLoader(clazz.getResource(resource));
        Parent root = null;
        try {
            log.info("[{}] Loading data", clazz);
            loader.setController(controller);
            root = loader.load();
            log.debug("[{}] Parent loaded", clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }
}
