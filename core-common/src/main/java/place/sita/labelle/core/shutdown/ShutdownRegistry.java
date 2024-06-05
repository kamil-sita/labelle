package place.sita.labelle.core.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShutdownRegistry {

	private static final Logger log = LoggerFactory.getLogger(ShutdownRegistry.class);

	private final List<Shutdownable> shutdownables = new ArrayList<>();

	public ShutdownRegistry(List<Shutdownable> shutdownables) {
		this.shutdownables.addAll(shutdownables);
	}

	public void register(Shutdownable shutdownable) {
		shutdownables.add(shutdownable);
	}

	public void shutdown() {
		shutdownables.forEach(shutdownable -> {
			try {
				shutdownable.shutdown();
			} catch (Exception e) {
				log.error("Error shutting down {}", shutdownable, e);
			}
		});
	}
}
