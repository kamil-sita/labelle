package place.sita.labelle.gui.local.fx.modulefx;

public interface ChildrenFactory {

	<T> T create(Class<T> clazz);

}
