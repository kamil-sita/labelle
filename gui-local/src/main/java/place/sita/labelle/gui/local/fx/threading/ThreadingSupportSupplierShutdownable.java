package place.sita.labelle.gui.local.fx.threading;

import org.springframework.stereotype.Component;
import place.sita.labelle.core.shutdown.Shutdownable;
import place.sita.modulefx.threading.ThreadingSupportSupplier;

@Component
public class ThreadingSupportSupplierShutdownable implements Shutdownable {
	@Override
	public void shutdown() {
		ThreadingSupportSupplier.shutdown();
	}
}
