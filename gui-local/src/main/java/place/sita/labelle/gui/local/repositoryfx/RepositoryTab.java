package place.sita.labelle.gui.local.repositoryfx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.gui.local.fx.ButtonCell;
import place.sita.labelle.gui.local.fx.LabPaginatorFactory;
import place.sita.labelle.gui.local.fx.LabPaginatorFactory.LabPaginator;
import place.sita.labelle.gui.local.fx.threading.Threading;
import place.sita.labelle.core.filtering.LogicalExpr;
import place.sita.labelle.core.images.loading.ImageCachingLoader;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.InRepositoryService.TagResponse;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.core.utils.Result2;
import place.sita.modulefx.annotations.FxChild;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.gui.local.fx.functional.FxFunctionalUi.ifSelected;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(order = 3, tabName = "Repository", resourceFile = "/fx/repository/repository.fxml")
public class RepositoryTab {

    private static final Logger log = LoggerFactory.getLogger(RepositoryTab.class);

    private final RepositoryService repositoryService;
    private final InRepositoryService inRepositoryService;
    private final ApplicationContext context;

    public RepositoryTab(RepositoryService repositoryService, InRepositoryService inRepositoryService, ApplicationContext context, ImageCachingLoader imageCachingLoader) {
        this.repositoryService = repositoryService;
        this.inRepositoryService = inRepositoryService;
	    this.context = context;
	    this.imageCachingLoader = imageCachingLoader;
    }

    private int pageSize = 200;

    @FXML
    private TextField idTextField;

    @FXML
    private TextField parentIdTextField;

    @FXML
    private ChoiceBox<Repository> repositoryChoiceBox;

    @FXML
    private Pagination paginator;

    @FXML
    private TableView<?> markersTable;

    @FXML
    private TableView<TagResponse> tagsTable;

    @FXML
    private CheckBox sharedCheckBox;

    @FXML
    private TextField tagEntryTextField;

    @FXML
    private TextField tagFamilyTextField;

    @FXML
    private TextField entryFamilyTextField;

    @FXML
    private TextField markerFamilyTextField;

    @FXML
    private AnchorPane imageDisplay;

    @FxChild(patchNode = "imageDisplay")
    private ScalableImageDisplayController scalableImageDisplayController;

    @FXML
    private AnchorPane persistentIdComponent;

    @FxChild(patchNode = "persistentIdComponent")
    private PersistentIdController persistentIdController;

    @FXML
    private AnchorPane deltasComponent;

    @FxChild(patchNode = "deltasComponent")
    private DeltasComponentController deltasComponentController;

    @PostFxConstruct
    public void setupRepositories() {
        ObservableList<Repository> repositories = FXCollections.observableArrayList();
        repositories.addAll(repositoryService.getRepositories(LogicalExpr.all()));
        Platform.runLater(() -> {
            repositoryChoiceBox.setItems(repositories);
        });
    }

    private LabPaginator<ImageResponse, FilteringParameters> labPaginator;

    private record FilteringParameters(UUID repositoryId) {

    }

    private final ImageCachingLoader imageCachingLoader;
    private final ExecutorService executors = Executors.newFixedThreadPool(1);

    private ImageResponse selectedImage;

    @PostFxConstruct
    public void setupOnChangeOfRepository() {
        labPaginator = LabPaginatorFactory.factory(
            paginator,
            pageSize,
            filteringParameters ->  inRepositoryService.count(getRepositoryId(filteringParameters), ""),
            (paging, filtering) ->  inRepositoryService.images().process().filterByRepository(getRepositoryId(filtering)).getPage(new Page(paging.offset(), paging.pageSize())).getAll(),
            selected -> {
                this.selectedImage = selected;
                loadImage(selected);
                loadTags(selected);
                pass(selected);
            }
        );

        repositoryChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Repository>() {
            @Override
            public void changed(ObservableValue<? extends Repository> observable, Repository oldValue, Repository newValue) {
                selectedRepository = newValue;
                labPaginator.hardReload(new FilteringParameters(newValue.id()));
            }
        });
    }

    private void pass(ImageResponse selected) {
        deltasComponentController.onImageSelected(selected);
    }

    private Future<Result2<BufferedImage, Exception>> currentFutureImage;
    private void loadImage(ImageResponse selected) {
        if (currentFutureImage != null) {
            currentFutureImage.cancel(true);
        }
        currentFutureImage = imageCachingLoader.load(selected.toPtr());
        executors.submit(() -> {
            Result2<BufferedImage, Exception> result;
            try {
                result = currentFutureImage.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            if (currentFutureImage.isCancelled()) {
                return;
            }
            if (result.isSuccess()) {
                try {
                    scalableImageDisplayController.set(result.getSuccess());
                } catch (Exception e) {
                    log.error("Couldn't set image", e);
                }
            }
        });
    }

    private ObservableList<TagResponse> tagsTableData = FXCollections.observableArrayList();

    @FXML
    private TableColumn<TagResponse, String> tagsFamilyColumn;

    @FXML
    private TableColumn<TagResponse, String> tagsValueColumn;

    @FXML
    private TableColumn<TagResponse, String> tagXColumn;

    @PostFxConstruct
    public void setupTagsTable() {
        tagsTable.setItems(tagsTableData);
        tagsFamilyColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().family()));
        tagsValueColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().tag()));
        tagXColumn.setCellFactory(cb -> {
            return new ButtonCell<>("X", tr -> {
                Threading.onSeparateThread(toolkit -> {
                   inRepositoryService.removeTag(selectedImage.id(), selectedRepository.id(), tr.tag(), tr.family());
                   toolkit.onFxThread(() -> {
                       tagsTableData.remove(tr);
                   });
                });
            });
        });
    }

    private final Threading.KeyStone keyStone = Threading.keyStone();

    private void loadTags(ImageResponse selected) {
        Threading.onSeparateThread(keyStone, toolkit -> {
            List<TagResponse> tags = inRepositoryService.getTags(selected.id());
            toolkit.onFxThread(() -> {
                tagsTableData.clear();
                tagsTableData.addAll(tags);
            });
        });
    }

    private UUID getRepositoryId(FilteringParameters filtering) {
        if (filtering == null) {
            return null;
        }
        return filtering.repositoryId;
    }

    private Repository selectedRepository;

    @FXML
    public void onAddImageAction(ActionEvent event) {
        ifSelected(repositoryChoiceBox)
            .then(repo -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                File file = fileChooser.showOpenDialog(getWindow());
                // todo this can be null
                inRepositoryService.addImage(repo.id(), file)
                    .onSuccess(image -> {
                        onUserAdded(List.of(image));
                    })
                    .onFailure1(dnmar -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "This file does not match any roots");
                        alert.show();
                    });
            });
    }


    @FXML
    public void onAddSyntheticImageAction(ActionEvent event) {
        ifSelected(repositoryChoiceBox)
            .then(repo -> {
                ImageResponse response = inRepositoryService.addEmptySyntheticImageWrap(repo.id());
                onUserAdded(List.of(response));
            });
    }

    private void onUserAdded(List<ImageResponse> image) {
        if (!image.isEmpty()) {
            ImageResponse first = image.get(0);
            labPaginator.insertSelectInto(inRepositoryService.images().process().filterByRepository(selectedRepository.id()).indexOf(first), first);
        }
    }

    private Window getWindow() {
        return repositoryChoiceBox.getScene().getWindow();
    }


    @FXML
    void addMarkerPress(ActionEvent event) {

    }

    @FXML
    void addTagPress(ActionEvent event) {
        String familyText = tagFamilyTextField.getText();
        String tagText = tagEntryTextField.getText();
        Threading.onSeparateThread(toolkit -> {
            inRepositoryService.addTag(selectedImage.id(), selectedRepository.id(), tagText, familyText);
            toolkit.onFxThread(() -> {
                tagsTableData.add(new TagResponse(tagText, familyText));
            });
        });
    }

    @FXML
    void bulkAddButtonPress(ActionEvent event) {
        ifSelected(repositoryChoiceBox)
            .then(repo -> {
                FileChooser fileChooser = new FileChooser();
                // todo webp seems to be not supported
                fileChooser.setTitle("Open Directory File");
                List<File> files = fileChooser.showOpenMultipleDialog(getWindow());

                Threading.onSeparateThread(toolkit -> {
                    // todo any error handling here could be useful. This probably should be a long running job.
                    List<ImageResponse> added = new ArrayList<>();
                    int failures = 0;
                    for (var file : files) {
                        var res = inRepositoryService.addImage(repo.id(), file);
                        if (res.isSuccess()) {
                            added.add(res.getSuccess());
                        } else {
                            failures++;
                        }
                    }
                        int finalFailures = failures;
                        toolkit.onFxThread(() -> {
                        onUserAdded(added);
                        if (finalFailures != 0) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, finalFailures + " failures encountered");
                            alert.show();
                        }
                    });
                });
            });
    }

    @FXML
    void removeButtonPress(ActionEvent event) {

    }

    @FXML
    void removeMarkerPress(ActionEvent event) {

    }

    @FXML
    void removeTagPress(ActionEvent event) {

    }

    @FXML
    void updateMarkerPress(ActionEvent event) {

    }

    @FXML
    void updateTagPress(ActionEvent event) {

    }

}
