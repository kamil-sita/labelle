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
import place.sita.modulefx.UnstableSceneReporter;
import place.sita.modulefx.ChildrenFactory;
import place.sita.modulefx.FxControllerLoader;
import place.sita.modulefx.FxSceneBuilderProcessors;
import place.sita.modulefx.threading.ThreadingSupportSupplier;
import place.sita.labelle.gui.local.menu.Menu;
import place.sita.modulefx.vtg.VirtualTreeGroup;

import java.util.*;

@Component
public class StageConfiguration {

	private final ConfigurableApplicationContext applicationContext;
	private final ShutdownRegistry shutdownRegistry;

	private final List<ExistingStage> stages = new ArrayList<>();
	private final ChildrenFactory childrenFactory;

	public StageConfiguration(ConfigurableApplicationContext applicationContext,
	                          ShutdownRegistry shutdownRegistry, ChildrenFactory childrenFactory) {
		this.applicationContext = applicationContext;
		this.shutdownRegistry = shutdownRegistry;
		this.childrenFactory = childrenFactory;
	}

	public UnstableSceneReporter configureTestStage(Stage stage) {
		return configureStage(stage, StageType.TEST);
	}

	public UnstableSceneReporter configureStage(Stage stage, StageType stageType) {
		if (stageType != StageType.ADDITIONAL) {
			System.setProperty("java.awt.headless", "false");
		}

		stage.setTitle("Labelle");
		//TransitTheme transitTheme = new TransitTheme(com.pixelduke.transit.Style.LIGHT);
		JMetro jMetro = new JMetro(Style.DARK);
		UnstableSceneReporter unstableSceneReporter = new UnstableSceneReporter();

		FxSceneBuilderProcessors processors = new FxSceneBuilderProcessors(childrenFactory, unstableSceneReporter);
		UUID loadId = UUID.randomUUID();
		unstableSceneReporter.markUnstable(loadId, "Loading new stage");

		VirtualTreeGroup virtualTreeGroup = new VirtualTreeGroup();

		Menu menu = applicationContext.getBean(Menu.class);
		Node node;
		try {
			node = FxControllerLoader.setupForController(menu, "/fx/mainmenu.fxml", processors, virtualTreeGroup);
		} finally {
			unstableSceneReporter.markStable(loadId);
		}

		Scene scene = new Scene((Parent) node, 1200, 800);
		//transitTheme.setScene(scene);
		jMetro.setScene(scene);
		stage.setScene(scene);
		scene.getStylesheets().add("dark_metro_labelle.css");

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

		return unstableSceneReporter;
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
