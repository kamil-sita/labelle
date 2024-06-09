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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.shutdown.ShutdownRegistry;
import place.sita.labelle.gui.local.fx.modulefx.FxControllerLoader;
import place.sita.labelle.gui.local.fx.threading.ThreadingSupportSupplier;
import place.sita.labelle.gui.local.menu.Menu;
import place.sita.labelle.gui.local.tab.ApplicationTab;
import place.sita.labelle.gui.local.tab.TabRegistrar;
import place.sita.labelle.gui.local.tab.UnloadAware;

import java.util.*;

@Component
public class StageConfiguration {

	private final List<ApplicationTab> applicationTabs;
	private final List<TabRegistrar> tabRegistrars;
	private final Menu menu;
	private final FxControllerLoader fxControllerLoader;
	private final ConfigurableApplicationContext applicationContext;
	private final ShutdownRegistry shutdownRegistry;

	private final List<ExistingStage> stages = new ArrayList<>();

	public StageConfiguration(List<ApplicationTab> applicationTabs,
	                          List<TabRegistrar> tabRegistrars,
	                          Menu menu,
	                          FxControllerLoader fxControllerLoader,
	                          ConfigurableApplicationContext applicationContext,
	                          ShutdownRegistry shutdownRegistry) {
		this.applicationTabs = applicationTabs;
		this.tabRegistrars = tabRegistrars;
		this.menu = menu;
		this.fxControllerLoader = fxControllerLoader;
		this.applicationContext = applicationContext;
		this.shutdownRegistry = shutdownRegistry;
	}

	public void configureTestStage(Stage stage) {
		configureStage(stage, StageType.TEST);
	}

	public void configureStage(Stage stage, StageType stageType) {
		if (stageType != StageType.ADDITIONAL) {
			System.setProperty("java.awt.headless", "false");
		}

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

		menu.mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
			menu.mainTabPane.getTabs().add(applicationTab.tab());
		});

		UUID id = UUID.randomUUID();
		ExistingStage thisStage = new ExistingStage(id, stage);
		stages.add(thisStage);
		stage.show();

		if (stageType != StageType.TEST) {
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent t) {
					stages.remove(thisStage);
					if (stages.isEmpty()) {
						ThreadingSupportSupplier.shutdown();
						Platform.exit();
						shutdownRegistry.shutdown();
						applicationContext.close();
					}
				}
			});
		}
	}

	public enum StageType {
		FIRST,
		ADDITIONAL,
		TEST,
		;
	}

	private record ExistingStage(UUID id, Stage stage) {

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ExistingStage that = (ExistingStage) o;
			return Objects.equals(id, that.id);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}
	}

}
