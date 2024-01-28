package place.sita.labelle.gui.local.fx;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.util.function.Consumer;

public class ButtonCell<T, Y> extends TableCell<T, Y> {

	private Button button;

	public ButtonCell(String text, Consumer<T> onClick) {
		button = new Button(text);
		button.setOnAction(event -> {
			T item = getTableRow().getItem();
			onClick.accept(item);
		});
	}

	@Override
	protected void updateItem(Y item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setGraphic(button);
		} else {
			setGraphic(null);
		}
	}
}
