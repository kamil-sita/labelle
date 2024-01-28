package place.sita.labelle.gui.local.fx.registrar;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import place.sita.labelle.gui.local.fx.FxControllerLoader;
import place.sita.labelle.gui.local.fx.LazyLoadable;
import place.sita.labelle.gui.local.menu.UnloadingTab;
import place.sita.labelle.gui.local.tab.ApplicationTab;
import place.sita.labelle.gui.local.tab.TabRegistrar;
import place.sita.modulefx.annotations.FxTab;

import java.util.ArrayList;
import java.util.List;

@Component
public class FxTabRegistrar implements TabRegistrar {

    private final ApplicationContext applicationContext;
    private final FxControllerLoader fxControllerLoader;

    public FxTabRegistrar(ApplicationContext applicationContext, FxControllerLoader fxControllerLoader) {
        this.applicationContext = applicationContext;
	    this.fxControllerLoader = fxControllerLoader;
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

                    return fxControllerLoader.setupForController(controller, annotation.resourceFile());
                };

                return new UnloadingTab<>(className, tabSupplier, annotation.tabName(), annotation.order());
            } else {
                throw new RuntimeException("No FxTab annotation present in a situation where it should: " + clazz);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
