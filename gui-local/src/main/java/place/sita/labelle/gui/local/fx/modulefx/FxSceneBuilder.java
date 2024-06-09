package place.sita.labelle.gui.local.fx.modulefx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FxSceneBuilder {
    private static final Logger log = LoggerFactory.getLogger(FxSceneBuilder.class);

    public static Node setupFxView(Object controller, String resource) {
        try {
            return getParent(controller, resource);
        } catch (Exception e) {
            throw new SetupException(getDetails(controller, resource) + ". Does file \"" + resource + "\" exist?", e);
        }
    }

    private static String getDetails(Object controller, String resource) {
        return "Exception when creating for controller: \"" + controller.getClass().getName() +
                "\" and resource \"" + resource + "\"";
    }

    private static Parent getParent(Object controller, String resource) {
        Class<?> clazz = controller.getClass();
        log.debug("[{}], Setting up FX view", clazz);
        log.debug("[{}] Creating parent loader", clazz);
        FXMLLoader loader = new FXMLLoader(clazz.getResource(resource));
        Parent root = null;
        try {
            log.debug("[{}] Loading data", clazz);
            loader.setController(controller);
            root = loader.load();
            log.debug("[{}] Parent loaded", clazz);
        } catch (IOException e) {
            throw new SetupException(getDetails(controller, resource), e);
        } catch (Exception e) {
            throw new SetupException(getDetails(controller, resource) + ". Does file \"" + resource + "\" exist?", e);
        }
        return root;
    }

    public static class SetupException extends RuntimeException {
        public SetupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
