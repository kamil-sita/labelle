package place.sita.modulefx;

import java.util.List;

public interface ChildrenFactory {

	<T> T create(Class<T> clazz);

	<T>  List<Class<?>> getClasses(Class<T> clazz);

}
