package place.sita.labelle.gui.local.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import place.sita.modulefx.threading.Threading;
import place.sita.modulefx.threading.Threading.KeyStone;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static place.sita.labelle.gui.local.fx.PaginationUtil.getNumberOfPages;

// todo: does this component has too much logic? Not enough logic? Is the logic separation here good?
public class LabPaginatorFactory {

    private static final Logger logger = LoggerFactory.getLogger(LabPaginatorFactory.class);


    public static <T, CtxT> LabPaginator<T, CtxT> factory(Pagination pagination,
                                                 int pageSize,
                                                 Function<CtxT, Integer> countFunction,
                                                 BiFunction<Paging, CtxT, List<T>> elemsFunction,
                                                 Consumer<T> onSelected) {
        return new LabPaginatorImpl<>(pagination, pageSize, countFunction, elemsFunction, onSelected);
    }

    public interface LabPaginator<T, CtxT> {

        void hardReload(CtxT ctxT);

        void insertSelectInto(int position, T element);

    }

    private static class LabPaginatorImpl<T, CtxT> implements LabPaginator<T, CtxT> {

        private final Pagination pagination;
        private final int pageSize;
        private final Function<CtxT, Integer> countFunction;
        private final BiFunction<Paging, CtxT, List<T>> elemsFunction;
        private final Consumer<T> onSelected;


        private LabPaginatorImpl(Pagination pagination,
                                 int pageSize,
                                 Function<CtxT, Integer> countFunction,
                                 BiFunction<Paging, CtxT, List<T>> elemsFunction,
                                 Consumer<T> onSelected) {
            this.pagination = pagination;
            this.pageSize = pageSize;
            this.countFunction = countFunction;
            this.elemsFunction = elemsFunction;
            this.onSelected = onSelected;
            init();
        }

        private void init() {
            hardReload(null);
        }

        private ListView<T> currentListView;

        private void createPageFactory(CtxT ctxT) {
            pagination.setPageFactory(param -> {
                StackPane stackPane = new StackPane();
                ProgressIndicator spinner = new ProgressIndicator(-1);
                ListView<T> listView = new ListView<>();
                stackPane.getChildren().addAll(listView, spinner);
                // todo rewrite this to threading framework
                Task<Void> loadDataTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            var els = elemsFunction.apply(new Paging(pageSize, pageSize * param), ctxT);

                            Platform.runLater(() -> {
                                listView.setItems(FXCollections.observableList(els));
                                spinner.setVisible(false);
                            });
                        } catch (Exception e) {
                            logger.error("Error loading data", e);
                        }
                        return null;
                    }
                };
                listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, nv) -> {
                    onSelected.accept(nv);
                });
                new Thread(loadDataTask).start();
                currentListView = listView;
                return stackPane;
            });
        }

        private final KeyStone keyStone = Threading.keyStone();

        @Override
        public void hardReload(CtxT ctxT) {
            Threading.onFxThread(keyStone, toolkit -> {
                pagination.setPageCount(Pagination.INDETERMINATE);
                pagination.setDisable(true);
                pagination.setPageFactory(param -> {
                    return new ProgressIndicator(-1);
                });
                toolkit.onSeparateThread(() -> {
                    int pageCount = getNumberOfPages(countFunction.apply(ctxT), pageSize);
                    toolkit.onFxThread(() -> {
                        pagination.setPageCount(pageCount);
                        pagination.setDisable(false);
                        createPageFactory(ctxT);
                    });
                });
            });

            Platform.runLater(() -> {
                new Thread(() -> {
                }).start();
            });
        }

        @Override
        public void insertSelectInto(int position, T element) {
            int pageOfPosition = position / pageSize;
            int page = pagination.getCurrentPageIndex();
            int positionOnPage = position % pageSize;
            if (page == pageOfPosition) {
                currentListView.getItems().add(positionOnPage, element);
                if (currentListView.getItems().size() > pageSize) {
                    currentListView.getItems().remove(pageSize);
                }
                currentListView.getSelectionModel().select(positionOnPage);
                currentListView.scrollTo(positionOnPage);
            } else {
                pagination.setCurrentPageIndex(pageOfPosition);
                Threading.onSeparateThread(toolkit -> {
	                try {
                        // not sure why it's needed. Not a big deal.
		                Thread.sleep(10);
	                } catch (InterruptedException e) {
		                throw new RuntimeException(e);
	                }
                    toolkit.onFxThread(() -> {
                        currentListView.getSelectionModel().select(positionOnPage);
                        currentListView.scrollTo(positionOnPage);
                    });
                });
            }
        }
    }

    public record Paging(int pageSize, int offset) {

    }

}
