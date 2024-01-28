package place.sita.labelle.core.automation.tasks.createchild;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateChildRepositoryTaskInput(
        List<UUID> parents,
        @Nullable UUID newRepositoryId,
        String newRepositoryName) {
}
