package place.sita.labelle.core.repository.taskapi;

import org.springframework.stereotype.Component;
import place.sita.magicscheduler.scheduler.ApiRegistrar.ApiRegistration;

@Component
public class RepositoryApiSupport implements ApiRegistration<RepositoryApi> {

	private final RepositoryApi repositoryApi;

	public RepositoryApiSupport(RepositoryApi repositoryApi) {
		this.repositoryApi = repositoryApi;
	}

	@Override
	public Class<RepositoryApi> typeClass() {
		return RepositoryApi.class;
	}

	@Override
	public RepositoryApi getInstance() {
		return repositoryApi;
	}
}
