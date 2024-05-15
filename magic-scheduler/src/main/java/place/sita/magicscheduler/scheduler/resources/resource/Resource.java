package place.sita.magicscheduler.scheduler.resources.resource;

public interface Resource<ResourceManagerTaskApiT> {

    String getDescription();

    Class<ResourceManagerTaskApiT> apiType();

}
