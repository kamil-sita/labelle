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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.shutdown.ShutdownRegistry;
import place.sita.modulefx.UnstableSceneReporter;
import place.sita.modulefx.ChildrenFactory;
import place.sita.modulefx.FxControllerLoader;
import place.sita.modulefx.FxSceneBuilderProcessors;
import place.sita.modulefx.threading.ThreadingSupportSupplier;
import place.sita.labelle.gui.local.menu.Menu;
import place.sita.modulefx.vtg.VirtualTreeGroup;
import place.sita.modulefx.vtg.VirtualTreeGroupElement;

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

	@EventListener
	public void handleEvent(Object event) {
		for (ExistingStage stage : stages) {
			stage.eventListener().handle(event);
		}
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

		VirtualTreeGroupElement el = new VirtualTreeGroupElement();
		virtualTreeGroup.addElement(el);

		StageEventListener eventListener = new StageEventListener() {
			@Override
			public void handle(Object event) {
				virtualTreeGroup.message(el.getId(), event);
			}
		};

		UUID id = UUID.randomUUID();
		ExistingStage thisStage = new ExistingStage(id, stage, eventListener);
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

	private interface StageEventListener {
		void handle(Object event);
	}

	private record ExistingStage(UUID id, Stage stage, StageEventListener eventListener) {

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
