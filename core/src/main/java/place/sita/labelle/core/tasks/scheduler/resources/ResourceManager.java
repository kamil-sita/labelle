package place.sita.labelle.core.tasks.scheduler.resources;

import place.sita.labelle.core.tasks.scheduler.resources.resource.Resource;

import java.util.UUID;

public interface ResourceManager {

    LockResult tryLock(UUID lockId, Resource<?> resource);

    LockResult canBeLocked(Resource<?> resource);

    void unlock(UUID lockId);

}
