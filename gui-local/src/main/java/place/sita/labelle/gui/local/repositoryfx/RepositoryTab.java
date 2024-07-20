package place.sita.labelle.gui.local.repositoryfx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jooq.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.images.loading.ImageCachingLoader;
import place.sita.labelle.core.images.loading.ImagePtrToFile;
import place.sita.labelle.core.jooq.StringBuilderErrorListener;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.image.ImageRepository;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.core.utils.Result2;
import place.sita.labelle.datasource.Page;
import place.sita.labelle.gui.local.fx.LabPaginatorFactory;
import place.sita.labelle.gui.local.fx.LabPaginatorFactory.LabPaginator;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.annotations.*;
import place.sita.modulefx.messagebus.MessageSender;
import place.sita.modulefx.threading.Threading;
import place.sita.modulefx.threading.Threading.KeyStone;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.gui.local.fx.functional.FxFunctionalUi.ifSelected;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(order = 3, tabName = "Repository", resourceFile = "/fx/repository/repository.fxml")
public class RepositoryTab implements MainMenuTab {

    private static final Logger log = LoggerFactory.getLogger(RepositoryTab.class);

    private final RepositoryService repositoryService;
    private final InRepositoryService inRepositoryService;
    private final ApplicationContext context;
    private final ImageCachingLoader imageCachingLoader;

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
    private TextField entryCategoryTextField;

    @FXML
    private TextField markerCategoryTextField;

    @FXML
    private AnchorPane imageDisplay;

    @FxChild(patchNode = "imageDisplay")
    private ScalableImageDisplayController scalableImageDisplayController;

    @FXML
    private AnchorPane persistentIdComponent;

    @FxChild(patchNode = "persistentIdComponent")
    private PersistentIdController persistentIdController;

    @FXML
    private AnchorPane tagsPane;

    @FxChild(patchNode = "tagsPane")
    private RepositoryTagController repositoryTagController;

    @FXML
    private AnchorPane deltasComponent;

    @FxChild(patchNode = "deltasComponent")
    private DeltasComponentController deltasComponentController;

    @FXML
    private TextArea filteringTextArea;

    @FXML
    private TextField filteringTextAreaFeedback;

    @PostFxConstruct
    public void setupRepositories() {
        ObservableList<Repository> repositories = FXCollections.observableArrayList();
        repositories.addAll(repositoryService.getRepositories());
        Platform.runLater(() -> {
            repositoryChoiceBox.setItems(repositories);
        });
    }

    private LabPaginator<ImageResponse, FilteringParameters> labPaginator;

    private record FilteringParameters(UUID repositoryId, String query) {

    }

    @PostFxConstruct
    public void setupOnChangeOfRepository() {
        labPaginator = LabPaginatorFactory.factory(
            paginator,
            pageSize,
            filteringParameters ->  inRepositoryService.images().process()
                .filterByRepository(getRepositoryId(filteringParameters))
                .process()
                .filterUsingTfLang(validatedQuery)
                .count(),
            (paging, filtering) ->  inRepositoryService.images().process()
                .filterByRepository(getRepositoryId(filtering))
                .process()
                .filterUsingTfLang(validatedQuery)
                .getPage(new Page(paging.offset(), paging.pageSize())).getAll(),
            selected -> {
                broadcastSelected(selected);
                loadImage(selected);
                pass(selected);
            }
        );

        repositoryChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Repository>() {
            @Override
            public void changed(ObservableValue<? extends Repository> observable, Repository oldValue, Repository newValue) {
                selectedRepository = newValue;
                labPaginator.hardReload(new FilteringParameters(selectedRepository.id(), validatedQuery));
            }
        });
    }

    @ModuleFx
    private MessageSender messageSender;

    private void broadcastSelected(ImageResponse selected) {
        if (selected == null) {
            messageSender.send(new ImageSelectedEvent(null));
        } else {
            messageSender.send(new ImageSelectedEvent(selected.id()));
        }
    }

    private void pass(ImageResponse selected) {
        deltasComponentController.onImageSelected(selected);
    }

    private Future<Result2<BufferedImage, Exception>> currentFutureImage;

    private final KeyStone loadImageKeyStone = Threading.keyStone();

    private void loadImage(ImageResponse selected) {
        scalableImageDisplayController.clear();
        var optionalFile = ImagePtrToFile.toFile(selected.toPtr());
        if (optionalFile.isPresent()) {
            scalableImageDisplayController.setFile(optionalFile.get());
        }
        if (currentFutureImage != null) {
            currentFutureImage.cancel(true);
        }
        currentFutureImage = imageCachingLoader.load(selected.toPtr());
        Threading.onSeparateThread(loadImageKeyStone, toolkit -> {
            if (currentFutureImage.isCancelled()) {
                return;
            }
            Result2<BufferedImage, Exception> result;
            try {
                result = currentFutureImage.get();
            } catch (CancellationException e) {
                return;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            if (currentFutureImage.isCancelled()) {
                return;
            }
            if (result.isSuccess()) {
                toolkit.onFxThread(() -> {
                    try {
                        BufferedImage bufferedImage = result.getSuccess();
                        scalableImageDisplayController.set(bufferedImage);
                        if (bufferedImage != null) {
                            context.publishEvent(new ImageLoadedEvent());
                        }
                    } catch (Exception e) {
                        log.error("Couldn't set image", e);
                    }
                });
            }
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

    @FxMessageListener
    public void onRequestSelect(SelectImageEvent selectImageEvent) {
        ImageResponse imageResponse = inRepositoryService.images().process().filterByImageId(selectImageEvent.imageId()).getOne();
        labPaginator.insertSelectInto(inRepositoryService.images().process().filterByRepository(selectedRepository.id()).indexOf(imageResponse), imageResponse);
    }

    private void onUserAdded(List<ImageResponse> image) {
        if (!image.isEmpty()) {
            ImageResponse first = image.getFirst();
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
    void updateMarkerPress(ActionEvent event) {

    }

    private final KeyStone parseFilterKeyStone = Threading.keyStone();

    private String validatedQuery;

    @FXML
    public void filteringTextAreaOnKeyTyped(KeyEvent event) {
        validatedQuery = null;
        String query = filteringTextArea.getText();
        if (query == null || query.isBlank()) {
            filteringTextAreaFeedback.setText("Validation OK");
            validatedQuery = query;
            labPaginator.hardReload(new FilteringParameters(selectedRepository.id(), validatedQuery));
            return;
        }
        Threading.onSeparateThread(parseFilterKeyStone, toolkit -> {
            StringBuilder errors = new StringBuilder();
            boolean generated = false;
            Exception exception = null;
            try {
                Condition condition = ImageRepository.parseToCondition(query, new StringBuilderErrorListener(errors));
                if (condition != null) {
                    generated = true;
                }
            } catch (Exception e) {
                exception = e;
            }
            if (!errors.isEmpty()) {
                toolkit.onFxThread(() -> {
                    filteringTextAreaFeedback.setText("Validation failed: " + errors);
                });
            } else if (exception != null) {
                Exception finalException = exception;
                toolkit.onFxThread(() -> {
                    filteringTextAreaFeedback.setText("Validation failed: " + finalException.getClass() + ", " + finalException.getMessage());
                });
            } else if (!generated) {
                toolkit.onFxThread(() -> {
                    filteringTextAreaFeedback.setText("Validation failed: No condition generated");
                });
            } else {
                toolkit.onFxThread(() -> {
                    filteringTextAreaFeedback.setText("Validation OK");
                    validatedQuery = query;
                    labPaginator.hardReload(new FilteringParameters(selectedRepository.id(), validatedQuery));
                });
            }
        });
    }

}
