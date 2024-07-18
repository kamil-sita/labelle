package place.sita.modulefx.messagebus;

import place.sita.modulefx.annotations.FxMessageListener;

/**
 * An instance of it might be injected if you use {@link place.sita.modulefx.annotations.ModuleFx} on enabled
 * bean. In particular, methods annotated with {@link FxMessageListener} might be called when a message is sent.
 */
public interface MessageSender {

	void send(Object message);

}
