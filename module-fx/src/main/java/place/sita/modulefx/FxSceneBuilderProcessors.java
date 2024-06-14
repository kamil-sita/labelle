package place.sita.modulefx;

import place.sita.modulefx.processors.*;
import place.sita.modulefx.processors.tabs.InjectTabsProcessor;

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
		processors.add(new MessageBusSupportProcessor());
	}

	public void add(FxSceneBuilderProcessor processor) {
		processors.add(processor);
	}

	public void runAll(FxSetupContext context) {
		processors.forEach(p -> p.process(context));
	}
}
