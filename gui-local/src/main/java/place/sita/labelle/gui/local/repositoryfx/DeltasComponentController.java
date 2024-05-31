package place.sita.labelle.gui.local.repositoryfx;

import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.annotations.FxNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/repository_deltas.fxml")
public class DeltasComponentController {

	private final InRepositoryService inRepositoryService;

	public DeltasComponentController(InRepositoryService inRepositoryService,
	                                 ImageDeltasComponentController imageDeltasComponentController,
	                                 TagDeltasComponentController tagDeltasComponentController) {
		this.inRepositoryService = inRepositoryService;
		this.imageDeltasComponentController = imageDeltasComponentController;
		this.tagDeltasComponentController = tagDeltasComponentController;
	}

	private ImageResponse selected;

	public void onImageSelected(ImageResponse selected) {
		this.selected = selected;
		tagDeltasComponentController.onImageSelected(selected);
	}

	@FXML
	private AnchorPane deltasImageAnchorPane;

	@FxChild(patchNode = "deltasImageAnchorPane")
	private final ImageDeltasComponentController imageDeltasComponentController;

	@FXML
	private AnchorPane deltasTagAnchorPane;

	@FxChild(patchNode = "deltasTagAnchorPane")
	private final TagDeltasComponentController tagDeltasComponentController;

	@FXML
	void applyAllEnabledDeltasButtonPress(ActionEvent event) {

	}

	@FXML
	void calculateAllEnabledDeltasButtonPress(ActionEvent event) {

	}

}
