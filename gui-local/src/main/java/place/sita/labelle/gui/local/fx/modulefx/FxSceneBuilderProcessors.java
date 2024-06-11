package place.sita.labelle.gui.local.fx.modulefx;

import place.sita.labelle.gui.local.fx.UnstableSceneReporter;
import place.sita.labelle.gui.local.fx.modulefx.processors.*;
import place.sita.labelle.gui.local.fx.modulefx.processors.tabs.InjectTabsProcessor;

import java.util.ArrayList;
import java.util.List;

public class FxSceneBuilderProcessors {

	private final List<FxSceneBuilderProcessor> processors = new ArrayList<>();

	public FxSceneBuilderProcessors(ChildrenFactory childrenFactory, UnstableSceneReporter unstableSceneReporter) {
		processors.add(new CheckSetHeightCorrelationProcessor());
		processors.add(new InjectParentFieldProcessor());
		processors.add(new InjectChildrenProcessor(childrenFactory));
		processors.add(new InjectTabsProcessor(childrenFactory, unstableSceneReporter));
		processors.add(new PostFxInjectProcessor());
	}

	public void add(FxSceneBuilderProcessor processor) {
		processors.add(processor);
	}

	public void runAll(FxSetupContext context) {
		processors.forEach(p -> p.process(context));
	}
}
