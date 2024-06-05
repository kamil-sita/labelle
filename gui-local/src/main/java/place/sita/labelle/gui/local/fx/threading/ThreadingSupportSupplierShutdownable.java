package place.sita.labelle.gui.local.fx.threading;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.shutdown.Shutdownable;

@Component
public class ThreadingSupportSupplierShutdownable implements Shutdownable {
	@Override
	public void shutdown() {
		ThreadingSupportSupplier.shutdown();
	}
}
