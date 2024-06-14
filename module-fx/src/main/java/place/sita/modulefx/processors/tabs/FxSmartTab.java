package place.sita.modulefx.processors.tabs;

import javafx.scene.control.Tab;
import place.sita.modulefx.UnstableSceneReporter;

public interface FxSmartTab {

	Tab tab();

	void load(UnstableSceneReporter unstableSceneReporter);

	void unload();

}
