package place.sita.labelle.gui.local.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import place.sita.labelle.gui.local.fx.threading.Threading;
import place.sita.labelle.gui.local.fx.threading.Threading.KeyStone;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static place.sita.labelle.gui.local.fx.PaginationUtil.getNumberOfPages;

public class LabPaginatorFactory {


    public static <T, CtxT> LabPaginator<CtxT> factory(Pagination pagination,
                                                 int pageSize,
                                                 Function<CtxT, Integer> countFunction,
                                                 BiFunction<Paging, CtxT, List<T>> elemsFunction,
                                                 Consumer<T> onSelected) {
        return new LabPaginatorImpl<>(pagination, pageSize, countFunction, elemsFunction, onSelected);
    }

    public interface LabPaginator<CtxT> {

        void hardReload(CtxT ctxT);

    }

    private static class LabPaginatorImpl<T, CtxT> implements LabPaginator<CtxT> {

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

        private void createPageFactory(CtxT ctxT) {
            pagination.setPageFactory(param -> {
                StackPane stackPane = new StackPane();
                ProgressIndicator spinner = new ProgressIndicator(-1);
                ListView<T> listView = new ListView<>();
                stackPane.getChildren().addAll(listView, spinner);
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
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, nv) -> {
                    onSelected.accept(nv);
                });
                new Thread(loadDataTask).start();
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
    }

    public record Paging(int pageSize, int offset) {

    }

}
