package place.sita.labelle.gui.local.repositoryfx;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.gui.local.fx.ButtonCell;
import place.sita.modulefx.annotations.FxMessageListener;
import place.sita.modulefx.annotations.FxNode;
import place.sita.modulefx.annotations.PostFxConstruct;
import place.sita.modulefx.threading.Threading;

import java.util.List;
import java.util.UUID;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/repository_tags.fxml")
public class RepositoryTagController {

	private final InRepositoryService inRepositoryService;

	public RepositoryTagController(InRepositoryService inRepositoryService) {
		this.inRepositoryService = inRepositoryService;
	}

	@FXML
	private TableView<Tag> tagsTable;

	private ObservableList<Tag> tagsTableData = FXCollections.observableArrayList();

	@FXML
	private TableColumn<Tag, String> tagsCategoryColumn;

	@FXML
	private TableColumn<Tag, String> tagsTagColumn;

	@FXML
	private TableColumn<Tag, String> tagXColumn;

	@FXML
	private TextField tagTagTextField;

	@FXML
	private TextField tagCategoryTextField;

	@PostFxConstruct
	public void setupTagsTable() {
		tagsTable.setItems(tagsTableData);
		tagsCategoryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().category()));
		tagsTagColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().tag()));
		tagXColumn.setCellFactory(cb -> {
			return new ButtonCell<>("X", tr -> {
				Threading.onSeparateThread(toolkit -> {
					inRepositoryService.removeTag(selectedImageId, null, tr);
					toolkit.onFxThread(() -> {
						tagsTableData.remove(tr);
					});
				});
			});
		});
	}

	private final Threading.KeyStone loadTagsKeyStone = Threading.keyStone();

	private UUID selectedImageId;

	@FxMessageListener
	public void onImageSelected(ImageSelectedEvent selected) {
		this.selectedImageId = selected.imageId();
		loadTags(selected.imageId());
	}

	private void loadTags(UUID imageId) {
		Threading.onSeparateThread(loadTagsKeyStone, toolkit -> {
			List<Tag> tags = inRepositoryService.getTags(imageId);
			toolkit.onFxThread(() -> {
				tagsTableData.clear();
				tagsTableData.addAll(tags);
			});
		});
	}

	@FXML
	public void addTagPress(ActionEvent event) {
		String categoryText = tagCategoryTextField.getText();
		String tagText = tagTagTextField.getText();
		Threading.onSeparateThread(toolkit -> {
			inRepositoryService.addTag(selectedImageId, new Tag(categoryText, tagText));
			toolkit.onFxThread(() -> {
				tagsTableData.add(new Tag(categoryText, tagText));
			});
		});
	}

	@FXML
	public void updateTagPress(ActionEvent event) {

	}

}
