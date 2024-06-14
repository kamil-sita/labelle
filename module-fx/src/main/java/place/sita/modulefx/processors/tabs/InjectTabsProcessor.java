package place.sita.modulefx.processors.tabs;

import javafx.scene.control.TabPane;
import place.sita.modulefx.ChildrenFactory;
import place.sita.modulefx.FxSceneBuilderProcessor;
import place.sita.modulefx.FxSetupContext;
import place.sita.modulefx.UnstableSceneReporter;
import place.sita.modulefx.annotations.FxInjectTabs;
import place.sita.modulefx.annotations.FxTab;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InjectTabsProcessor implements FxSceneBuilderProcessor {

	private final ChildrenFactory childrenFactory;
	private final UnstableSceneReporter unstableSceneReporter;

	public InjectTabsProcessor(ChildrenFactory childrenFactory, UnstableSceneReporter unstableSceneReporter) {
		this.childrenFactory = childrenFactory;
		this.unstableSceneReporter = unstableSceneReporter;
	}

	@Override
	public void process(FxSetupContext context) {
		Class<?> controllerClass = context.controller().getClass();

		Arrays.stream(controllerClass.getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(FxInjectTabs.class))
			.forEach(field -> {
				injectTabs(context.controller(), field, context);
			});
	}

	private void injectTabs(Object controller, Field field, FxSetupContext context) {
		FxInjectTabs tabsConfig = field.getAnnotation(FxInjectTabs.class);
		Class<?> filter = tabsConfig.value();

		List<Class<?>> classes = childrenFactory.getClasses(filter);
		List<TabInfo> tabInfos = fetchTabInfoOrder(classes);

		TabPane tabPane = getTabPane(controller, field);
		FxSmartTabManager manager = new FxSmartTabManager(context.virtualTreeGroup(), unstableSceneReporter);

		for (TabInfo tabInfo : tabInfos) {
			FxTab fxTab = tabInfo.clazz.getAnnotation(FxTab.class);
			if (fxTab == null) {
				throw new RuntimeException("Cannot inject something that is not an @FxTab");
			}
			FxSmartTab tab = FxSmartTabFactory.create(fxTab, tabInfo.clazz, context, childrenFactory);
			manager.add(tab);
		}

		manager.register(tabPane);
	}

	private static TabPane getTabPane(Object controller, Field field) {
		TabPane tabPane;
		try {
			field.setAccessible(true);
			tabPane = (TabPane) field.get(controller);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return tabPane;
	}

	private List<TabInfo> fetchTabInfoOrder(List<Class<?>> classes) {
		List<TabInfo> tabInfos = new ArrayList<>();

		for (Class<?> clazz : classes) {
			FxTab tab = clazz.getAnnotation(FxTab.class);
			if (tab == null) {
				throw new RuntimeException("Cannot inject something that is not an @FxTab");
			}
			tabInfos.add(new TabInfo(tab.order(), clazz));
		}
		tabInfos.sort(Comparator.comparingInt(TabInfo::order));

		return tabInfos;
	}

	private record TabInfo(int order, Class<?> clazz) {}

}
