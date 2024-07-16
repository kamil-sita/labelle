package place.sita.modulefx.processors.tabs;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import place.sita.modulefx.UnstableSceneReporter;
import place.sita.modulefx.threading.Threading;
import place.sita.modulefx.vtg.VirtualTreeGroup;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FxSmartTabManager {
	private static final Logger log = LoggerFactory.getLogger(FxSmartTabManager.class);

	private final Map<String, FxSmartTab> tabs = new LinkedHashMap<>();
	private final Map<String, VirtualTreeGroup> virtualTreeGroups = new HashMap<>();

	private final VirtualTreeGroup parentVtg;
	private final UnstableSceneReporter unstableSceneReporter;

	public FxSmartTabManager(VirtualTreeGroup parentVtg, UnstableSceneReporter unstableSceneReporter) {
		this.parentVtg = parentVtg;
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
					VirtualTreeGroup group = virtualTreeGroups.get(oldValue.getId());
					if (group == null) {
						log.warn("Tried to remove group of {}, but its seemingly not registered", oldValue.getId());
					} else {
						parentVtg.removeChild(group.id());
					}
				}
				if (newValue != null) {
					FxSmartTab newTab = tabs.get(newValue.getId());
					VirtualTreeGroup group = newTab.load(unstableSceneReporter);
					virtualTreeGroups.put(newValue.getId(), group);
					parentVtg.addChild(group);
				}
			});
		});

		Tab firstTab = tabPane.getTabs().get(0);
		String firstTabId = firstTab.getId();
		FxSmartTab firstFxTab = tabs.get(firstTabId);
		VirtualTreeGroup group = firstFxTab.load(unstableSceneReporter);
		virtualTreeGroups.put(firstTabId, group);
		parentVtg.addChild(group);
	}
}
