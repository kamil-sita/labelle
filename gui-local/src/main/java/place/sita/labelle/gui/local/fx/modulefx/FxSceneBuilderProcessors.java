package place.sita.labelle.gui.local.fx.modulefx;

import place.sita.labelle.gui.local.fx.modulefx.processors.CheckSetHeightCorrelationProcessor;
import place.sita.labelle.gui.local.fx.modulefx.processors.InjectChildrenProcessor;
import place.sita.labelle.gui.local.fx.modulefx.processors.InjectParentFieldProcessor;
import place.sita.labelle.gui.local.fx.modulefx.processors.PostFxInjectProcessor;

import java.util.ArrayList;
import java.util.List;

public class FxSceneBuilderProcessors {

	private final List<FxSceneBuilderProcessor> processors = new ArrayList<>();

	public FxSceneBuilderProcessors(ChildrenFactory childrenFactory) {
		processors.add(new CheckSetHeightCorrelationProcessor());
		processors.add(new InjectParentFieldProcessor());
		processors.add(new InjectChildrenProcessor(childrenFactory));
		processors.add(new PostFxInjectProcessor());
	}

	public void add(FxSceneBuilderProcessor processor) {
		processors.add(processor);
	}

	public void runAll(FxSetupContext context) {
		processors.forEach(p -> p.process(context));
	}
}
