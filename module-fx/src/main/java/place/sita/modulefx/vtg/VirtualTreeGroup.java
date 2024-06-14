package place.sita.modulefx.vtg;

import place.sita.modulefx.BadApiUsageException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualTreeGroup {

	private final UUID id = UUID.randomUUID();
	private VirtualTreeGroup parent;
	private final List<VirtualTreeGroup> children = new ArrayList<>();
	private final List<VirtualTreeGroupElement> group = new ArrayList<>();

	public UUID id() {
		return id;
	}

	public void addChild(VirtualTreeGroup node) {
		if (node.parent != null) {
			throw new BadApiUsageException("Node already has a parent");
		}
		node.parent = this;
		children.add(node);
	}

	public void removeChild(UUID id) {
		VirtualTreeGroup child = children.stream().filter(n -> n.id().equals(id)).findFirst().orElse(null);

		if (child == null) {
			throw new BadApiUsageException("Not a child of this group");
		}

		child.parent = null;
		children.remove(child);
	}

	/**
	 * Passes message to all other connected nodes
	 */
	public void message(UUID senderId, Object message) {
		for (VirtualTreeGroupElement element : group) {
			if (element.getId().equals(senderId)) {
				continue;
			}
			element.receive(message);
		}

		if (parent != null && !parent.id().equals(senderId)) {
			parent.message(id, message);
		}

		for (VirtualTreeGroup child : children) {
			if (!child.id().equals(senderId)) {
				child.message(id, message);
			}
		}
	}

	public void addElement(VirtualTreeGroupElement element) {
		group.add(element);
	}

}
