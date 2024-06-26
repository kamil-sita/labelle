package place.sita.magicscheduler.scheduler;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.shutdown.Shutdownable;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class MagicSchedulerBackendImpl implements MagicSchedulerBackend, Shutdownable {

	private final ThreadPoolExecutor executorService;

	public MagicSchedulerBackendImpl() {
		executorService = new ThreadPoolExecutor(
			1,
			4,
			2000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>()
		);
		executorService.allowCoreThreadTimeOut(true);
	}

	@Override
	public void runLater(Runnable runnable) {
		executorService.execute(runnable);
	}

	@Override
	public void shutdown() {
		executorService.shutdown();
	}
}
