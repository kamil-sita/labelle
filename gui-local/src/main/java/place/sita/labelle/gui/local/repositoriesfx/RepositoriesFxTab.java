package place.sita.labelle.gui.local.repositoriesfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static place.sita.labelle.gui.local.fx.functional.FxFunctionalUi.ifSelected;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import place.sita.labelle.gui.local.menu.MainMenuTab;
import place.sita.modulefx.annotations.FxTab;
import place.sita.modulefx.annotations.PostFxConstruct;

import java.util.List;
import java.util.Objects;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
@FxTab(resourceFile = "/fx/repositories.fxml", order = 1, tabName = "Repositories")
public class RepositoriesFxTab implements MainMenuTab {

    private final RepositoryService repositoryService;
    public RepositoriesFxTab(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    private ObservableList<Repository> observableRepositories;
    private ObservableList<Repository> observableChildren;
    private ObservableList<Repository> observableParents;
    private ObservableList<Repository> observableOtherRepos;

    @PostFxConstruct
    public void setupList() {
        List<Repository> repositories = repositoryService.getRepositories();
        observableRepositories = FXCollections.observableArrayList(repositories);
        repositoryList.setItems(observableRepositories);

        observableChildren = FXCollections.observableArrayList();
        childrenList.setItems(observableChildren);
        observableParents = FXCollections.observableArrayList();
        parentsList.setItems(observableParents);
        observableOtherRepos = FXCollections.observableArrayList();
        addAsParentChildList.setItems(observableOtherRepos);
    }

    @PostFxConstruct
    public void onRepoSelect() {
        // todo rewrite it to not crash on nothing selected as a general util.
        repositoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            refreshViewWithCurrentSelected(newValue);
        });
    }


    ///

    @FXML
    private TextField nameField;

    @FXML
    private ListView<Repository> addAsParentChildList;

    @FXML
    private ListView<Repository> childrenList;

    @FXML
    private ListView<Repository> parentsList;

    @FXML
    private ListView<Repository> repositoryList;

    @FXML
    private TextField repositoryNameTextField;

    @FXML
    private TextField uuidField;

    @FXML
    void addAsChildButtonPress(ActionEvent event) {
        ifSelected(addAsParentChildList)
            .then(candidate -> {
                ifSelected(repositoryList)
                    .then(repo -> {
                        repositoryService.addParentChild(candidate.id(), repo.id());
                        refreshViewWithCurrentSelected(repo);
                    });
            });
    }

    @FXML
    void addAsParentButtonPress(ActionEvent event) {
        ifSelected(addAsParentChildList)
            .then(candidate -> {
                ifSelected(repositoryList)
                    .then(repo -> {
                       repositoryService.addParentChild(repo.id(), candidate.id());
                       refreshViewWithCurrentSelected(repo);
                    });
            });
    }

    @FXML
    void addNewButtonPress(ActionEvent event) {
        String name = repositoryNameTextField.getText();
        Repository repository = repositoryService.addRepository(name);
        observableRepositories.add(repository);
    }

    @FXML
    void deleteRepositoryButtonPress(ActionEvent event) {
        ifSelected(repositoryList)
            .then(repo -> {
               repositoryService.deleteRepository(repo.id());
               int id = observableRepositories.indexOf(repo);
               observableRepositories.remove(id);
            });
    }

    @FXML
    void removeChildrenButtonPress(ActionEvent event) {
        ifSelected(childrenList)
            .then(childRepo -> {
                ifSelected(repositoryList)
                    .then(parentRepo -> {
                        repositoryService.removeParentChild(childRepo.id(), parentRepo.id());
                        refreshViewWithCurrentSelected(parentRepo);
                    });
            });
    }

    @FXML
    void removeParentButtonPress(ActionEvent event) {
        ifSelected(parentsList)
            .then(parentRepo -> {
                ifSelected(repositoryList)
                    .then(childRepo -> {
                        repositoryService.removeParentChild(childRepo.id(), parentRepo.id());
                        refreshViewWithCurrentSelected(childRepo);
                    });
            });
    }

    @FXML
    void updateNameButtonPress(ActionEvent event) {
        ifSelected(repositoryList)
            .then(repo -> {
                Repository newRepo = repositoryService.rename(repo.id(), repositoryNameTextField.getText());
                int idx = observableRepositories.indexOf(repo);
                observableRepositories.set(idx, newRepo);
                refreshViewWithCurrentSelected(newRepo);
            });
    }

    private void refreshViewWithCurrentSelected(Repository newValue) {
        if (newValue != null) {
            repositoryNameTextField.setText(newValue.name());
            uuidField.setText(newValue.id().toString());
            nameField.setText(newValue.name());
            List<Repository> children = repositoryService.getChildren(newValue.id());
            List<Repository> parents = repositoryService.getParents(newValue.id());
            observableParents.setAll(parents);
            observableChildren.setAll(children);
            List<Repository> allReposButThis = observableRepositories
                    .stream()
                    .filter(repo -> !Objects.equals(repo.id(), newValue.id()))
                    .toList();
            observableOtherRepos.setAll(allReposButThis);
        } else {
            repositoryNameTextField.clear();
            uuidField.clear();
            nameField.clear();
            observableParents.clear();
            observableChildren.clear();
            observableOtherRepos.clear();
        }
    }

}
