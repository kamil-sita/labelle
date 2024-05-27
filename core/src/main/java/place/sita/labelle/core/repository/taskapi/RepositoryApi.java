package place.sita.labelle.core.repository.taskapi;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.acrossrepository.AcrossRepositoryService;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.repositories.RepositoryService;

@Component
public class RepositoryApi {

	private final RepositoryService repositoryService;
	private final InRepositoryService inRepositoryService;
	private final AcrossRepositoryService acrossRepositoryService;


	public RepositoryApi(RepositoryService repositoryService,
	                     InRepositoryService inRepositoryService,
	                     AcrossRepositoryService acrossRepositoryService) {
		this.repositoryService = repositoryService;
		this.inRepositoryService = inRepositoryService;
		this.acrossRepositoryService = acrossRepositoryService;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public InRepositoryService getInRepositoryService() {
		return inRepositoryService;
	}

	public AcrossRepositoryService getAcrossRepositoryService() {
		return acrossRepositoryService;
	}
}
