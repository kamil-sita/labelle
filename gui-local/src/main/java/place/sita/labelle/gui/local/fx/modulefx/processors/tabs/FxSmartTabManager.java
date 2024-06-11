package place.sita.labelle.gui.local.fx.modulefx.processors.tabs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import place.sita.labelle.gui.local.fx.UnstableSceneReporter;
import place.sita.labelle.gui.local.fx.threading.Threading;

import java.util.HashMap;
import java.util.Map;

public class FxSmartTabManager {

	private final Map<String, FxSmartTab> tabs = new HashMap<>();

	private final UnstableSceneReporter unstableSceneReporter;

	public FxSmartTabManager(UnstableSceneReporter unstableSceneReporter) {
		this.unstableSceneReporter = unstableSceneReporter;
	}

	public void add(FxSmartTab tab) {
		if (tabs.containsKey(tab.tab().getId())) {
			throw new IllegalArgumentException("Tab with id " + tab.tab().getId() + " already exists");
		}
		tabs.put(tab.tab().getId(), tab);
	}

	public void register(TabPane tabPane) {
		tabs.forEach((id, tab) -> tabPane.getTabs().add(tab.tab()));

		Threading.KeyStone keyStone = Threading.keyStone();

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Threading.onSeparateThread(keyStone, toolkit -> {
				if (oldValue != null) {
					FxSmartTab oldTab = tabs.get(oldValue.getId());
					oldTab.unload();
				}
				if (newValue != null) {
					FxSmartTab newTab = tabs.get(newValue.getId());
					newTab.load(unstableSceneReporter);
				}
			});
		});

		Tab firstTab = tabPane.getTabs().get(0);
		FxSmartTab firstFxTab = tabs.get(firstTab.getId());
		firstFxTab.load(unstableSceneReporter);
	}
}
