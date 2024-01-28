package place.sita.labelle.gui.local.menu;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import place.sita.labelle.gui.local.fx.LazyLoadable;
import place.sita.labelle.gui.local.tab.ApplicationTab;
import place.sita.labelle.gui.local.tab.UnloadAware;


public class UnloadingTab<T extends LazyLoadable> implements ApplicationTab, UnloadAware {
    private static final Logger log = LoggerFactory.getLogger(UnloadingTab.class);

    private final Tab internalTab;
    private final String internalClassName;
    private final T lazyLoadable;
    private final int order;
    private boolean loaded = false;

    public UnloadingTab(String internalClassName, T lazyLoadable, String title, int order) {
        this.internalClassName = internalClassName;
        this.lazyLoadable = lazyLoadable;
        this.order = order;
        internalTab = new Tab(title);
        internalTab.setClosable(false);
    }

    @Override
    public Tab tab() {
        return internalTab;
    }

    public void load() {
        log.info("Loading class {}", internalClassName);
        if (loaded) {
            log.debug("Unloading previous version of this ({}) interface", internalClassName);
            unloadLoaded();
        } else {
            log.debug("Not unloading self ({}) as it shouldn't be loaded", internalClassName);
        }

        var component = lazyLoadable.getComponent();

        Platform.runLater(() -> {
            log.debug("Setting up new interface for {}", internalClassName);
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
            internalTab.setContent(anchorPane);
            log.debug("New interface done for {}", internalClassName);
            loaded = true;
        });
    }

    public void unload() {
        if (!loaded) {
            return;
        }
        unloadLoaded();
    }

    private void unloadLoaded() {
        Platform.runLater(() -> {
            internalTab.setContent(new ProgressIndicator(-1));
        });
        loaded = false;
    }

    @Override
    public int getOrder() {
        return order;
    }

}
