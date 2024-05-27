package place.sita.labelle.core.repository.inrepository;

import java.util.UUID;

public record Ids(UUID uniqueId, String persistentId, String parentPersistentId) {
}
