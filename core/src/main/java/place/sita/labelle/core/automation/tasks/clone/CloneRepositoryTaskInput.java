package place.sita.labelle.core.automation.tasks.clone;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public record CloneRepositoryTaskInput(
        UUID repositoryToClone,
        @Nullable UUID newRepositoryId,
        String newRepositoryName,
        Map<UUID, UUID> parentsRemapping) {
}
