package place.sita.modulefx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UnstableSceneReporter {

	private static final Logger log = LoggerFactory.getLogger(UnstableSceneReporter.class);
	private final Set<UUID> locksOnStableScene = new HashSet<>();
	private final Map<UUID, String> correlationMap = new HashMap<>();

	public void markUnstable(UUID id, String correlation) {
		synchronized (this) {
			boolean wasStable = locksOnStableScene.isEmpty();
			if (wasStable) {
				log.info("Scene is now unstable due to \"{}\"", correlation);
			} else {
				log.info("Scene is unstable due to a new event \"{}\" and {} other events", correlation, correlationMap.size() - 1);
			}
			locksOnStableScene.add(id);
			correlationMap.put(id, correlation);
		}
	}

	public void markStable(UUID id) {
		synchronized (this) {
			if (locksOnStableScene.contains(id)) {
				locksOnStableScene.remove(id);
				correlationMap.remove(id);
				if (locksOnStableScene.isEmpty()) {
					log.info("Scene is now stable");
				} else {
					log.info("Scene is still unstable due to {} other events", correlationMap.size());
				}
			} else {
				log.info("Scene was not marked as unstable with id {}, yet it was reported as stable", id);
			}
		}
	}

	public boolean isStable() {
		synchronized (this) {
			return locksOnStableScene.isEmpty();
		}
	}

}
