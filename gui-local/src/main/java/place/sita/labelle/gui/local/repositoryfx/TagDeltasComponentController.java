package place.sita.labelle.gui.local.repositoryfx;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.delta.DeltaService;
import place.sita.labelle.core.repository.inrepository.delta.TagDeltaType;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.gui.local.fx.ButtonCell;
import place.sita.modulefx.threading.Threading;
import place.sita.modulefx.annotations.FxNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.util.Set;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxNode(resourceFile = "/fx/repository/repository_deltas_tags.fxml")
public class TagDeltasComponentController {

	private final DeltaService deltaService;

	public TagDeltasComponentController(InRepositoryService inRepositoryService, DeltaService deltaService) {
		this.inRepositoryService = inRepositoryService;
		this.deltaService = deltaService;
	}

	private final InRepositoryService inRepositoryService;

	@FXML
	private TextField deltaEntryTextField;

	@FXML
	private TextField deltaFamilyTextField;

	public void onImageSelected(ImageResponse selected) {
		this.selected = selected;
		reloadDeltas();
		reloadParentTags();
	}

	/*
	 * Current delta table
	 */

	private ObservableList<DeltaResponse> tagsTableData = FXCollections.observableArrayList();

	@FXML
	private TableView<DeltaResponse> deltaTagsTableView;

	@FXML
	private TableColumn<DeltaResponse, String> deltaDeleteButtonTableColumn;

	@FXML
	private TableColumn<DeltaResponse, String> deltaTypeTableColumn;

	@FXML
	private TableColumn<DeltaResponse, String> deltaFamilyTableColumn;

	@FXML
	private TableColumn<DeltaResponse, String> deltaEntryTableColumn;

	private ImageResponse selected;

	private final Threading.KeyStone reloadDeltasKeyStone = Threading.keyStone();

	private void reloadDeltas() {
		tagsTableData.clear();

		Threading.onSeparateThread(reloadDeltasKeyStone, toolkit -> {
			var tdrs = inRepositoryService.tagDeltas().process().filterByImageId(selected.id()).getAll();
			toolkit.onFxThread(() -> {
				for (var tr : tdrs) {
					tagsTableData.add(new DeltaResponse(map(tr.type()), tr.tag(), tr.family()));
				}
			});
		});
	}

	private DeltaType map(TagDeltaType type) {
		return switch (type) {
			case ADD -> DeltaType.ADD;
			case REMOVE -> DeltaType.REMOVE;
		};
	}

	private TagDeltaType map(DeltaType type) {
		return switch (type) {
			case ADD -> TagDeltaType.ADD;
			case REMOVE -> TagDeltaType.REMOVE;
		};
	}

	@PostFxConstruct
	public void initDeltas() {
		deltaTagsTableView.setItems(tagsTableData);

		deltaEntryTableColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().tag()));
		deltaFamilyTableColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().family()));
		deltaTypeTableColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().type().toString()));
		deltaDeleteButtonTableColumn.setCellFactory(cb -> {
			return new ButtonCell<>("X", tr -> {
				Threading.onSeparateThread(toolkit -> {
					inRepositoryService.tagDeltas().process().filterByImageId(selected.id()).process().byTagDelta(map(tr.type()), tr.family, tr.tag).remove();
					toolkit.onFxThread(() -> {
						tagsTableData.remove(tr);
					});
				});
			});
		});
	}

	private record DeltaResponse(DeltaType type, String tag, String family) {

	}

	private enum DeltaType {
		ADD {
			@Override
			public String toString() {
				return "+";
			}
		},
		REMOVE {
			@Override
			public String toString() {
				return "-";
			}
		},
		;

	}

	@FXML
	void addDeltaButtonPress(ActionEvent event) {

	}

	@FXML
	void removeDeltaButtonPress(ActionEvent event) {

	}

	/*
	 * End of: Current delta table
	 */

	/*
	 * Parents table
	 */

	@FXML
	private TableView<ParentTags> parentTagsTableView;

	@FXML
	private TableColumn<ParentTags, String> parentsTagsEntryColumn;


	@FXML
	private TableColumn<ParentTags, String> parentsTagsFamilyColumn;

	private ObservableList<ParentTags> parentTagsData = FXCollections.observableArrayList();

	@PostFxConstruct
	public void initParents() {
		parentTagsTableView.setItems(parentTagsData);

		parentsTagsFamilyColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().family()));
		parentsTagsEntryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().value()));
	}

	private record ParentTags(String family, String value) {

	}



	private final Threading.KeyStone reloadParentTagsKeyStone = Threading.keyStone();

	private void reloadParentTags() {
		parentTagsData.clear();

		if (selected != null) {
			Threading.onSeparateThread(reloadParentTagsKeyStone, toolkit -> {
				var tdrs = inRepositoryService.parentTags(selected.id());
				toolkit.onFxThread(() -> {
					for (var tr : tdrs) {
						parentTagsData.add(new ParentTags(tr.family(), tr.tag()));
					}
				});
			});
		}
	}

	@FXML
	void applyTagsDeltaButtonPress(ActionEvent event) {

	}

	@FXML
	void calculateTagsDeltaButtonPress(ActionEvent event) {
		if (selected != null) {
			Threading.onSeparateThread(toolkit -> {

				deltaService.recalculateTagDeltas(Set.of(selected.id()));
				toolkit.onFxThread(this::reloadDeltas);
			});
		}
	}

	@FXML
	private CheckBox enableTagDeltaCheckBox;
}
