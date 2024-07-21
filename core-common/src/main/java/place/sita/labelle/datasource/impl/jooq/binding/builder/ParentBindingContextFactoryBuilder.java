package place.sita.labelle.datasource.impl.jooq.binding.builder;

import org.jooq.Table;
import place.sita.labelle.datasource.impl.jooq.binding.binding.BindingContextFactory;

public interface ParentBindingContextFactoryBuilder<TableT extends Table> extends BindingContextFactoryBuilder<TableT> {

	BindingContextFactory build();
}
