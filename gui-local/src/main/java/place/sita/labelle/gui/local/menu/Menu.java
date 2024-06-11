package place.sita.labelle.gui.local.menu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.scheduler.events.TaskExecutionCompleteEvent;
import place.sita.magicscheduler.scheduler.events.TaskPickedUpEvent;
import place.sita.labelle.gui.local.fx.threading.Threading;
import place.sita.modulefx.annotations.FxInjectTabs;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(scopeName = SCOPE_PROTOTYPE)
@Component
public class Menu {
	private static final Logger log = LoggerFactory.getLogger(Menu.class);

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Label statusLabel;

	@FXML
	@FxInjectTabs(MainMenuTab.class)
	public TabPane mainTabPane;

	private AtomicInteger scheduled = new AtomicInteger(0);
	private AtomicInteger done = new AtomicInteger(0);

	// todo 43 @EventListener
	public void onScheduled(TaskPickedUpEvent event) {
		possiblyRestartProgressBar();
		scheduled.incrementAndGet();
		updateProgressBar();
	}

	// todo 43 @EventListener
	public void onDone(TaskExecutionCompleteEvent event) {
		possiblyRestartProgressBar();
		done.incrementAndGet();
		updateProgressBar();
	}

	private void updateProgressBar() {
		Threading.onFxThread(toolkit -> {
			double progress = 1.0 * done.get() / scheduled.get();;
			progressBar.setProgress(progress);
			if (done.get() == scheduled.get()) {
				progressBar.setStyle("-fx-accent: green");
			} else {
				progressBar.setStyle("-fx-accent: blue");
			}
		});
	}

	private void possiblyRestartProgressBar() {
		if (scheduled.get() == done.get()) {
			scheduled.set(0);
			done.set(0);
		}
	}

}
