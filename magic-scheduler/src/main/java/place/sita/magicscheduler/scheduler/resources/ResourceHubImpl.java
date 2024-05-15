package place.sita.magicscheduler.scheduler.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.*;


@Component
public class ResourceHubImpl implements ResourceHub {

    private static final Logger logger = LoggerFactory.getLogger(ResourceHubImpl.class);

    private final Map<Resource<?>, ResourceManager> managers = new HashMap<>();
    private final Map<Resource<?>, ?> apis = new HashMap<>();
    private final Map<UUID, Set<Resource<?>>> lockedResourcesByLockId = new HashMap<>();

    @Override
    public LockResult tryLock(UUID lockId, Resource<?>... resources) {
        if (lockedResourcesByLockId.containsKey(lockId)) {
            throw new RuntimeException("Duplicate lockId");
        }
        synchronized (this) {
            Set<Resource<?>> lockedResources = new HashSet<>();
            boolean rollback = true;
            Resource<?> lastRes = null;
            try {
                for (Resource<?> resource : resources) {
                    lastRes = resource;
                    LockResult result = getManagerForResource(resource).tryLock(lockId, resource);
                    if (!result.isSuccess()) {
                        return result;
                    }
                    lockedResources.add(resource);
                }
                rollback = false;
                lockedResourcesByLockId.put(lockId, lockedResources);
                return new LockResult.LockedSuccessfully();
            } catch (Throwable e) {
                logger.error("Exception while trying to lock resource " + lastRes, e);
                return handleThrowable(e);
            } finally {
                if (rollback) {
                    for (Resource<?> lockedResource : lockedResources) {
                        getManagerForResource(lockedResource).unlock(lockId);
                    }
                }
            }
        }
    }

    private static LockResult.LockFailRetryLater handleThrowable(Throwable e) {
        if (e instanceof Error error) {
            throw error;
        }
        return new LockResult.LockFailRetryLater();
    }

    @Override
    public LockResult canBeLocked(Resource<?> resource) {
        try {
            return getManagerForResource(resource).canBeLocked(resource);
        } catch (Throwable e) {
            logger.error("Exception while trying to check lock of resource " + resource, e);
            return handleThrowable(e);
        }
    }

    @Override
    public void unlock(UUID lockId) {
        synchronized (this) {
            Set<Resource<?>> lockedResources = lockedResourcesByLockId.getOrDefault(lockId, Set.of());
            for (Resource<?> resource : lockedResources) {
                try {
                    getManagerForResource(resource).unlock(lockId);
                } catch (Throwable e) {
	                logger.error("Failed to release a resource {} due to an exception", resource, e);
                }
            }
        }
    }

    private ResourceManager getManagerForResource(Resource<?> resource) {
        ResourceManager resourceManager = managers.get(resource);
        if (resourceManager == null) {
            throw new RuntimeException("Cannot find resource manager for resource: " + resource);
        }
        return resourceManager;
    }

    @Override
    public <ApiT> void register(Resource<ApiT> resource, ResourceManager resourceManager, ApiT api) {
        if (managers.containsKey(resource)) {
            throw new RuntimeException("ResourceManager redefinition");
        }
        managers.put(resource, resourceManager);
        ((Map) apis).put(resource, api); // todo fixup
    }

    @Override
    public <ResourceApiT> ResourceApiT getApi(Resource<ResourceApiT> resource) {
        if (!apis.containsKey(resource)) {
            throw new RuntimeException("No API registered");
        }
        return (ResourceApiT) apis.get(resource);
    }
}
