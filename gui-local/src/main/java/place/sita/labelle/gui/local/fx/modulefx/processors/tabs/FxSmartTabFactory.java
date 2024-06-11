package place.sita.labelle.gui.local.fx.modulefx.processors.tabs;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import place.sita.labelle.gui.local.fx.UnstableSceneReporter;
import place.sita.labelle.gui.local.fx.modulefx.ChildrenFactory;
import place.sita.labelle.gui.local.fx.modulefx.FxSetupContext;
import place.sita.modulefx.annotations.FxTab;

import java.util.UUID;

public class FxSmartTabFactory {

	public static FxSmartTab create(FxTab fxTab, Class<?> tabClass, FxSetupContext setupContext, ChildrenFactory factory) {
		switch (fxTab.loadMode()) {
			case ONLY_WHEN_NEEDED:
				return createUnloadingTab(fxTab, tabClass, setupContext, factory);
			default:
				throw new IllegalArgumentException("Unknown load mode: " + fxTab.loadMode());
		}
	}

	private static FxSmartTab createUnloadingTab(FxTab fxTab, Class<?> tabClass, FxSetupContext setupContext, ChildrenFactory factory) {
		UUID id = UUID.randomUUID();
		String stringId = id.toString();

		Tab internalTab = new Tab(fxTab.tabName());
		internalTab.setId(stringId);
		internalTab.setClosable(false);

		Platform.runLater(() -> {
			internalTab.setContent(new ProgressIndicator(-1));
		});

		return new FxSmartTab() {
			private boolean loaded = false;
			@Override
			public Tab tab() {
				return internalTab;
			}

			@Override
			public void load(UnstableSceneReporter unstableSceneReporter) {
				if (loaded) {
					return;
				}

				UUID loadId = UUID.randomUUID();
				unstableSceneReporter.markUnstable(loadId, "Loading tab: " + tabClass.getName());

				Object bean = factory.create(tabClass);
				Node component = setupContext.setupForController(bean, fxTab.resourceFile(), setupContext);

				AnchorPane anchorPane = new AnchorPane();
				if (component instanceof Region region) {
					region.setMinWidth(100);
					region.setMinHeight(100);
				}
				anchorPane.getChildren().add(component);
				AnchorPane.setTopAnchor(component, 0.0);
				AnchorPane.setLeftAnchor(component, 0.0);
				AnchorPane.setRightAnchor(component, 0.0);
				AnchorPane.setBottomAnchor(component, 0.0);
				Platform.runLater(() -> {
					internalTab.setContent(anchorPane);
					unstableSceneReporter.markStable(loadId);
				});
				loaded = true;
			}

			@Override
			public void unload() {
				if (!loaded) {
					return;
				}

				Platform.runLater(() -> {
					internalTab.setContent(new ProgressIndicator(-1));
				});

				loaded = false;
			}
		};
	}

}