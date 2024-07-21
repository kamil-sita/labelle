package place.sita.labelle.datasource.impl.jooq.binding.binding;

import org.jooq.Condition;
import org.jooq.Table;

public interface PropertyBindingSubEntityContext<TableT extends Table> extends PropertyBindingContext {

	TableT getTable();

	Condition getTableBindCondition();

}
