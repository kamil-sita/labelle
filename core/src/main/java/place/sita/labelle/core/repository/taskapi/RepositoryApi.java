package place.sita.labelle.core.repository.taskapi;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.image.replication.ImageReplication;
import place.sita.labelle.core.repository.repositories.RepositoryService;

@Component
public class RepositoryApi {

	private final RepositoryService repositoryService;
	private final InRepositoryService inRepositoryService;
	private final ImageReplication imageReplication;


	public RepositoryApi(RepositoryService repositoryService,
	                     InRepositoryService inRepositoryService,
	                     ImageReplication imageReplication) {
		this.repositoryService = repositoryService;
		this.inRepositoryService = inRepositoryService;
		this.imageReplication = imageReplication;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public InRepositoryService getInRepositoryService() {
		return inRepositoryService;
	}

	public ImageReplication getImageReplication() {
		return imageReplication;
	}

}
