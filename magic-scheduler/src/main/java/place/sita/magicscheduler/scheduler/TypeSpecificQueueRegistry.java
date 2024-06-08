package place.sita.magicscheduler.scheduler;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TypeSpecificQueueRegistry {

	private final Map<String, TypeSpecificQueue> queues = new HashMap<>();

	public void register(String code, TypeSpecificQueue queue) {
		queues.put(code, queue);
	}

	public TypeSpecificQueue get(String code) {
		return queues.get(code);
	}

}
