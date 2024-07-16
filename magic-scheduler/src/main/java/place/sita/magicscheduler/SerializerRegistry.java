package place.sita.magicscheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

// workaround
/**
 * Registry that can be used when the current context knows that it has to serialize a parameter <b>T</b>,
 * for task with given code, but it doesn't have a reference to it.
 */
@Component
public class SerializerRegistry {

	private final Map<String, Function<Object, String>> registry = new HashMap();

	public <T> void register(String code, Function<T, String> serializer) {
		registry.put(code, (Function<Object, String>) serializer);
	}

	public <T> String convert(String code, T parameter) {
		if (!registry.containsKey(code)) {
			throw new NoDefinitionException("Cannot find task with code: " + code);
		}
		return registry.get(code).apply(parameter);
	}

}
