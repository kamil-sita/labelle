package place.sita.magicscheduler;

import java.util.List;
import java.util.UUID;

public interface InternalTaskSubmitter {
	UUID UUID_FOR_USER_SUBMITTED_TASKS = UUID.fromString("00000000-0000-0000-0000-000000000000");

	void submitTaskForLater(UUID id, String code, String parameter, UUID parent, List<UUID> requirements);

	default void submitTaskForLater(UUID id, String code, String parameter, UUID parent) {
		submitTaskForLater(id, code, parameter, parent, List.of());
	}

	default UUID submitTaskForLater(String code, String parameter, UUID parent) {
		UUID uuid = UUID.randomUUID();
		submitTaskForLater(uuid, code, parameter, parent);
		return uuid;
	}

}
