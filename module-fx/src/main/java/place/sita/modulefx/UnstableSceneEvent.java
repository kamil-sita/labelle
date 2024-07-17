package place.sita.modulefx;

import java.util.UUID;

public sealed interface UnstableSceneEvent {

	record MarkSceneAsUnstable(UUID correlationId, String correlation) implements UnstableSceneEvent {
	}

	record MarkSceneAsStable(UUID correlationId) implements UnstableSceneEvent {
	}

}
