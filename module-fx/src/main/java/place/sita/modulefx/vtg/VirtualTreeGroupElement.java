package place.sita.modulefx.vtg;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualTreeGroupElement {

	private final UUID id = UUID.randomUUID();
	private final List<MessageListener> listeners = new ArrayList<>();

	public UUID getId() {
		return id;
	}

	public void addListener(MessageListener listener) {
		listeners.add(listener);
	}

	public void receive(Object message) {
		for (MessageListener listener : listeners) {
			listener.receive(message);
		}
	}
}
