package place.sita.labelle.gui.local;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.FxControllerLoader;
import place.sita.labelle.gui.local.fx.threading.ThreadingSupportSupplier;
import place.sita.labelle.gui.local.menu.Menu;
import place.sita.labelle.gui.local.tab.ApplicationTab;
import place.sita.labelle.gui.local.tab.TabRegistrar;
import place.sita.labelle.gui.local.tab.UnloadAware;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class JavaFxAppReadyForStartEventListener implements ApplicationListener<JavaFxAppReadyForStartEvent> {

	private final List<ApplicationTab> applicationTabs;
	private final List<TabRegistrar> tabRegistrars;
	private final ApplicationContext applicationContext;
	private final place.sita.labelle.gui.local.menu.Menu menu;
	private final FxControllerLoader fxControllerLoader;

	public JavaFxAppReadyForStartEventListener(List<ApplicationTab> applicationTabs,
											   List<TabRegistrar> tabRegistrars,
											   ApplicationContext applicationContext,
											   Menu menu,
											   FxControllerLoader fxControllerLoader) {
		this.applicationTabs = applicationTabs;
		this.tabRegistrars = tabRegistrars;
		this.applicationContext = applicationContext;
		this.menu = menu;
		this.fxControllerLoader = fxControllerLoader;
	}

	@Override
	public void onApplicationEvent(JavaFxAppReadyForStartEvent event) {
		SplashScreen splashScreen = SplashScreen.getSplashScreen();
		if (splashScreen != null) {
			splashScreen.close();
		}

		Stage stage = event.getStage();
		stage.setTitle("Labelle");
		//TransitTheme transitTheme = new TransitTheme(com.pixelduke.transit.Style.LIGHT);
		JMetro jMetro = new JMetro(Style.DARK);
		Node node = fxControllerLoader.setupForController(menu, "/fx/mainmenu.fxml");

		Scene scene = new Scene((Parent) node, 1200, 800);
		//transitTheme.setScene(scene);
		jMetro.setScene(scene);
		stage.setScene(scene);
		scene.getStylesheets().add("dark_metro_labelle.css");

		List<ApplicationTab> allTabs = new ArrayList<>();
		for (var registrar : tabRegistrars) {
			allTabs.addAll(registrar.tabs());
		}
		allTabs.addAll(applicationTabs);

		allTabs.sort(Comparator.comparingInt(ApplicationTab::getOrder));

		menu.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			new Thread(() -> {
				for (var applicationTab : allTabs) {
					if (applicationTab instanceof UnloadAware unloadAware) {
						if (newValue != applicationTab.tab()) {
							unloadAware.unload();
						}
					}
				}
				for (var applicationTab : allTabs) {
					if (applicationTab instanceof UnloadAware unloadAware) {
						if (newValue == applicationTab.tab()) {
							unloadAware.load();
						}
					}
				}
			}).start();
		});
		allTabs.forEach(applicationTab -> {
			menu.tabPane.getTabs().add(applicationTab.tab());
		});

		stage.show();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				ThreadingSupportSupplier.shutdown();
				Platform.exit();
				event.getContext().close();
				System.exit(0); // todo closing all executorservices probably should've fixed it instead.
			}
		});
	}
}
