package place.sita.labelle.core.tasks.scheduler;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiRegistrar {

	private final List<ApiRegistration<?>> apiRegistrations;

	public ApiRegistrar(List<ApiRegistration<?>> apiRegistrations) {
		this.apiRegistrations = apiRegistrations;
	}

	public <T> T resolve(Class<T> clazz) {
		if (clazz == null || clazz == Void.class) {
			return null;
		}

		for (var apiRegistration : apiRegistrations) {
			// check if there's a class that implements what we want
			if (clazz.isAssignableFrom(apiRegistration.typeClass())) {
				return (T) apiRegistration.getInstance();
			}
		}
		throw new RuntimeException();
	}


	public interface ApiRegistration<T> {

		Class<T> typeClass();

		T getInstance();

	}

}
