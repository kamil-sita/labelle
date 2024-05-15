package place.sita.magicscheduler.scheduler.resources;

import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.UUID;

public interface ResourceManager {

    LockResult tryLock(UUID lockId, Resource<?> resource);

    LockResult canBeLocked(Resource<?> resource);

    void unlock(UUID lockId);

}
