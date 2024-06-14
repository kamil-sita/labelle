package place.sita.modulefx.vtg;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import place.sita.modulefx.BadApiUsageException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;

public class VirtualTreeGroupTest {

	@Test
	public void shouldReportAddingChildTwice() {
		// given
		VirtualTreeGroup group = new VirtualTreeGroup();
		VirtualTreeGroup child = new VirtualTreeGroup();
		group.addChild(child);

		// when
		assertThatThrownBy(() -> group.addChild(child))
			// then
			.isInstanceOf(BadApiUsageException.class)
			.hasMessage("Node already has a parent");
	}

	@Test
	public void shouldPassMessageToChildrenParentAndOtherElementsButNotSelf() {
		// given
		VirtualTreeGroup groupWithSelf = new VirtualTreeGroup();
		VirtualTreeGroup parent = new VirtualTreeGroup();
		VirtualTreeGroup child = new VirtualTreeGroup();
		parent.addChild(groupWithSelf);
		groupWithSelf.addChild(child);

		MessageListener listenerInParent = Mockito.mock(MessageListener.class);
		MessageListener otherListenerInMiddle = Mockito.mock(MessageListener.class);
		MessageListener selfListenerInMiddle = Mockito.mock(MessageListener.class);
		MessageListener listenerInChild = Mockito.mock(MessageListener.class);

		VirtualTreeGroupElement parentElement = new VirtualTreeGroupElement();
		parentElement.addListener(listenerInParent);
		parent.addElement(parentElement);

		VirtualTreeGroupElement otherElementInMiddle = new VirtualTreeGroupElement();
		otherElementInMiddle.addListener(otherListenerInMiddle);
		groupWithSelf.addElement(otherElementInMiddle);

		VirtualTreeGroupElement selfElementInMiddle = new VirtualTreeGroupElement();
		selfElementInMiddle.addListener(selfListenerInMiddle);
		groupWithSelf.addElement(selfElementInMiddle);

		VirtualTreeGroupElement childElement = new VirtualTreeGroupElement();
		childElement.addListener(listenerInChild);
		child.addElement(childElement);

		// when
		groupWithSelf.message(selfElementInMiddle.getId(), "message");

		// then
		Mockito.verify(listenerInParent).receive("message");
		Mockito.verify(otherListenerInMiddle).receive("message");
		Mockito.verify(selfListenerInMiddle, Mockito.never()).receive(anyString());
		Mockito.verify(listenerInChild).receive("message");
	}

	@Test
	public void shouldNotMessageRemovedChild() {
		// given
		VirtualTreeGroup group = new VirtualTreeGroup();
		VirtualTreeGroup child = new VirtualTreeGroup();
		group.addChild(child);

		MessageListener listener = Mockito.mock(MessageListener.class);
		VirtualTreeGroupElement element = new VirtualTreeGroupElement();
		element.addListener(listener);
		child.addElement(element);

		// when
		group.removeChild(child.id());
		group.message(element.getId(), "message");

		// then
		Mockito.verify(listener, Mockito.never()).receive("message");
	}

}
