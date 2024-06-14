package place.sita.modulefx.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.Executors.defaultThreadFactory;

public class ThreadingSupportSupplier {

	private static final ExecutorService executorService;

	static {
		executorService = Executors.newFixedThreadPool(4, r -> {
			return defaultThreadFactory().newThread(r);
		});
		setSupplier(runnable -> executorService.execute(runnable));
	}


	public interface RunLater {
		void runLater(Runnable run);
	}

	private static RunLater runLater;

	public static void setSupplier(RunLater runLater) {
		ThreadingSupportSupplier.runLater = runLater;
	}

	public static void doRunLater(Runnable runnable) {
		if (runLater == null) {
			throw new IllegalStateException("Not yet initialized");
		} else {
			runLater.runLater(runnable);
		}
	}

	public static void shutdown() {
		executorService.shutdown();
	}

}
