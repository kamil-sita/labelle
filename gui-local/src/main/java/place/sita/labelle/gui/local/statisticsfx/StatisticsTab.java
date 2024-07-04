package place.sita.labelle.gui.local.statisticsfx;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.statistics.StatisticsService;
import place.sita.labelle.core.repository.inrepository.statistics.TagWithCountResponse;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;
import place.sita.modulefx.threading.Threading;

import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;


@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/statistics.fxml", order = 1, tabName = "Statistics")
public class StatisticsTab implements MainMenuTab {

    private final StatisticsService statisticsService;
    private final RepositoryService repositoryService;

    @FXML
    private TableColumn<TableElement, String> categoryColumn;

    @FXML
    private TableColumn<TableElement, Number> countColumn;

    @FXML
    private ChoiceBox<Repository> repositoryChoiceBox;

    @FXML
    private TableView<TableElement> table;

    @FXML
    private TableColumn<TableElement, String> tagColumn;

	public StatisticsTab(StatisticsService statisticsService, RepositoryService repositoryService) {
		this.statisticsService = statisticsService;
		this.repositoryService = repositoryService;
	}

    @PostFxConstruct
    public void setupRepositories() {
        ObservableList<Repository> repositories = FXCollections.observableArrayList();
        repositories.addAll(repositoryService.getRepositories());
        Platform.runLater(() -> {
            repositoryChoiceBox.setItems(repositories);
        });
    }


    private final ObservableList<TableElement> tableElements = FXCollections.observableArrayList();

    @PostFxConstruct
    public void setupTable() {
        table.setItems(tableElements);
        categoryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().category()));
        tagColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().tag()));
        countColumn.setCellValueFactory(cellData -> new ReadOnlyIntegerWrapper(cellData.getValue().count()));
    }

    private final Threading.KeyStone keyStone = Threading.keyStone();

    @PostFxConstruct
    public void onRepoSelected() {
        repositoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            tableElements.clear();
            if (newValue == null) {
                return;
            }
            Threading.onSeparateThread(keyStone, toolkit -> {
                List<TagWithCountResponse> response = statisticsService.getTagCount(newValue.id());
                toolkit.onFxThread(() -> {
                    for (TagWithCountResponse tagWithCountResponse : response) {
                        tableElements.add(
                            new TableElement(tagWithCountResponse.tag().family(), tagWithCountResponse.tag().value(), tagWithCountResponse.count())
                        );
                    }
                });
            });
        });
    }

    private record TableElement(String category, String tag, int count) {

    }

}
