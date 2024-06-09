package place.sita.labelle.gui.local.fx.registrar;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.modulefx.ChildrenFactory;
import place.sita.labelle.gui.local.fx.modulefx.FxControllerLoader;
import place.sita.labelle.gui.local.fx.LazyLoadable;
import place.sita.labelle.gui.local.fx.UnstableSceneReporter;
import place.sita.labelle.gui.local.fx.modulefx.FxSceneBuilderProcessors;
import place.sita.labelle.gui.local.menu.UnloadingTab;
import place.sita.labelle.gui.local.tab.ApplicationTab;
import place.sita.labelle.gui.local.tab.TabRegistrar;
import place.sita.modulefx.annotations.FxTab;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FxTabRegistrar implements TabRegistrar {

    private final ApplicationContext applicationContext;
    private final UnstableSceneReporter unstableSceneReporter;
    private final ChildrenFactory childrenFactory;

    public FxTabRegistrar(
            ApplicationContext applicationContext,
            UnstableSceneReporter unstableSceneReporter,
            ChildrenFactory childrenFactory) {
        this.applicationContext = applicationContext;
	    this.unstableSceneReporter = unstableSceneReporter;
	    this.childrenFactory = childrenFactory;
    }

    @Override
    public List<ApplicationTab> tabs() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(FxTab.class));

        List<ApplicationTab> tabs = new ArrayList<>();

        for (BeanDefinition beanDef : provider.findCandidateComponents("place.sita.labelle.gui.local")) {

            tabs.add(makeIntoDefinition(beanDef));
        }

        return tabs;
    }

    private ApplicationTab makeIntoDefinition(BeanDefinition beanDef) {
        String className = beanDef.getBeanClassName(); // let's hope that this is the actual class, and not something else.
        // as it doesn't have to be, see javadoc

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(FxTab.class)) {
                FxTab annotation = clazz.getAnnotation(FxTab.class);

                LazyLoadable tabSupplier = () -> {
                    Object controller = applicationContext.getBean(clazz);

                    UUID loadId = UUID.randomUUID();
                    try {
                        unstableSceneReporter.markUnstable(loadId, "Loading tab " + annotation.tabName());
                        FxSceneBuilderProcessors processors = new FxSceneBuilderProcessors(childrenFactory);
                        return FxControllerLoader.setupForController(controller, annotation.resourceFile(), processors);
                    } finally {
                        unstableSceneReporter.markStable(loadId);
                    }
                };

                return new UnloadingTab<>(className, tabSupplier, annotation.tabName(), annotation.order(), unstableSceneReporter);
            } else {
                throw new RuntimeException("No FxTab annotation present in a situation where it should: " + clazz);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
