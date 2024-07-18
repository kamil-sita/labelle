package place.sita.labelle.gui.local.repositoryfx;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/scalable_image_display.fxml")
public class ScalableImageDisplayController {

    @FXML
    private ImageView imageView;


    @FXML
    private AnchorPane imageViewContainer;

    @FXML
    private Button openButton;

    @FXML
    private Button openFileExplorerButton;

    @PostFxConstruct
    public void scaleImgAutomatically() {
        imageView.fitWidthProperty().bind(imageViewContainer.widthProperty());
        imageView.fitHeightProperty().bind(imageViewContainer.heightProperty());

        imageView.setPreserveRatio(true);
    }

    private File file;

    public void setFile(File file) {
        this.file = file;
        openButton.setDisable(false);
        openFileExplorerButton.setDisable(false);
    }

    public void clear() {
        this.file = null;
        imageView.setImage(null);
        openButton.setDisable(true);
        openFileExplorerButton.setDisable(true);
    }

    public void set(BufferedImage bufferedImage) {
        javafx.scene.image.Image image = SwingFXUtils.toFXImage(bufferedImage, null);;
        imageView.setImage(image);
    }

    @FXML
    public void openButtonPress(ActionEvent event) {
	    try {
		    Desktop.getDesktop().open(file);
	    } catch (IOException e) {
		    throw new RuntimeException(e);
	    }
    }

    @FXML
    public void openFileExplorerButtonPress(ActionEvent event) {
        try {
            Desktop.getDesktop().open(file.getParentFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
