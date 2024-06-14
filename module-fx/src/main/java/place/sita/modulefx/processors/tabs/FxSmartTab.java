package place.sita.modulefx.processors.tabs;

import javafx.scene.control.Tab;
import place.sita.modulefx.UnstableSceneReporter;
import place.sita.modulefx.vtg.VirtualTreeGroup;

public interface FxSmartTab {

	Tab tab();

	VirtualTreeGroup load(UnstableSceneReporter unstableSceneReporter);

	void unload();

}
