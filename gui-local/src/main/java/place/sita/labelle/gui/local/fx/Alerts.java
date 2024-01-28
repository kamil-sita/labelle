package place.sita.labelle.gui.local.fx;

import javafx.scene.control.Alert;

public class Alerts {


    public static void error(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text);
        alert.show();
    }

}
