package place.sita.labelle.core.tasks.scheduler.resources.resource;

public interface Resource<ResourceManagerTaskApiT> {

    String getDescription();

    Class<ResourceManagerTaskApiT> apiType();

}
