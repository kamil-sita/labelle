package place.sita.labelle.gui.local.fx.modulefx;

import org.springframework.stereotype.Component;
import place.sita.modulefx.ModuleFxConfig;

@Component
public class LabelleModuleFxConfig implements ModuleFxConfig {
	@Override
	public String getPackage() {
		return "place.sita.labelle";
	}
}
