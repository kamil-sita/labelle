package place.sita.labelle.gui.local.fx.functional;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import place.sita.labelle.core.utils.ResultRouters;
import place.sita.labelle.core.utils.ResultRouters.ConditionalResult2;

public class FxFunctionalUi {

    public static <T, ResT extends ConditionalResult2<T, Void, ResT>> ResT ifSelected(ListView<T> tListView) {
        T t = tListView.getSelectionModel().getSelectedItem();
        if (t != null) {
            return ResultRouters.successHandler(t);
        } else {
            return ResultRouters.failureHandler(null);
        }
    }

    public static <T, ResT extends ConditionalResult2<T, Void, ResT>> ResT ifSelected(ChoiceBox<T> tChoiceBox) {
        T t = tChoiceBox.getSelectionModel().getSelectedItem();
        if (t != null) {
            return ResultRouters.successHandler(t);
        } else {
            return ResultRouters.failureHandler(null);
        }
    }
}
