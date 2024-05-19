package place.sita.labelle.gui.local.repositoryfx;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.awt.image.BufferedImage;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/scalable_image_display.fxml")
public class ScalableImageDisplayController {

    @FXML
    private ImageView imageView;


    @FXML
    private AnchorPane imageViewContainer;


    @PostFxConstruct
    public void scaleImgAutomatically() {
        imageView.fitWidthProperty().bind(imageViewContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageViewContainer.heightProperty());

        imageView.setPreserveRatio(true);
    }


    public void set(BufferedImage bufferedImage) {
        javafx.scene.image.Image image = SwingFXUtils.toFXImage(bufferedImage, null);;
        imageView.setImage(image);
    }

}
