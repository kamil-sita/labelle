package place.sita.labelle.datasource.impl.jooq.binding.binding;

import org.jooq.Table;

public interface BindingContextFactory<TableT extends Table> {

	PropertyBindingContext startContext(TableT table);

}
