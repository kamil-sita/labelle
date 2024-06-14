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
			}
			locksOnStableScene.add(id);
			correlationMap.put(id, correlation);
		}
	}

	public void markStable(UUID id) {
		synchronized (this) {
			locksOnStableScene.remove(id);
			correlationMap.remove(id);
			if (locksOnStableScene.isEmpty()) {
				log.info("Scene is now stable");
			}
		}
	}

	public boolean isStable() {
		synchronized (this) {
			return locksOnStableScene.isEmpty();
		}
	}

}
