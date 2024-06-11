package place.sita.labelle.gui.local.fx.modulefx.processors.tabs;

import javafx.scene.control.Tab;
import place.sita.labelle.gui.local.fx.UnstableSceneReporter;

public interface FxSmartTab {

	Tab tab();

	void load(UnstableSceneReporter unstableSceneReporter);

	void unload();

}
