package place.sita.labelle.gui.local.repositoryfx;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.modulefx.annotations.FxNode;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/repository_deltas.fxml")
public class DeltasComponentController {



}
