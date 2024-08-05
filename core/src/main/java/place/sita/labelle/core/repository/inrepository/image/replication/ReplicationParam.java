package place.sita.labelle.core.repository.inrepository.image.replication;

import java.util.Collection;
import java.util.UUID;

public sealed interface ReplicationParam<ExpectedResultT> {

	record Duplicate(UUID imageId) implements ReplicationParam<DuplicateImageId> {

	}

	record HardCopyToNewRepo(UUID sourceRepoId, UUID targetRepoId) implements ReplicationParam<Void> {

	}

	record FillChildRepo(Collection<UUID> parentRepoIds, UUID childRepoId) implements ReplicationParam<Void> {

	}

	//



	record DuplicateImageId(UUID imageId) {

	}

}
