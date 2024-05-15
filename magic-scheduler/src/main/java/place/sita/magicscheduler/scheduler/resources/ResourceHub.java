package place.sita.magicscheduler.scheduler.resources;

import place.sita.magicscheduler.scheduler.resources.resource.Resource;

import java.util.UUID;

public interface ResourceHub {
	LockResult tryLock(UUID lockId, Resource<?>... resources);

	LockResult canBeLocked(Resource<?> resource);

	void unlock(UUID lockId);

	<ApiT> void register(Resource<ApiT> resource, ResourceManager resourceManager, ApiT api);

	<ResourceApiT> ResourceApiT getApi(Resource<ResourceApiT> resource);
}
