package place.sita.labelle.datasource.impl.jooq.binding.binding;

import org.jooq.Field;

public interface PropertyBindingContext {

	Field getBinding(String path);

	PropertyBindingSubEntityContext<?> getSubEntityContext(String path);

}
