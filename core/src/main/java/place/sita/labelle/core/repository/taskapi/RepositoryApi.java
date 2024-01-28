package place.sita.labelle.core.repository.taskapi;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.repositories.RepositoryService;

@Component
public class RepositoryApi {

	private final RepositoryService repositoryService;
	private final InRepositoryService inRepositoryService;


	public RepositoryApi(RepositoryService repositoryService, InRepositoryService inRepositoryService) {
		this.repositoryService = repositoryService;
		this.inRepositoryService = inRepositoryService;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public InRepositoryService getInRepositoryService() {
		return inRepositoryService;
	}
}
