package place.sita.labelle.datasource.impl.jooq.binding.builder;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface BindingContextFactoryBuilder<TableT extends Table> {

	void addField(String path, Function<TableT, Field> field);

	<NewTableT extends Table> BindingContextFactoryBuilder<NewTableT> addSubEntity(String path, Function<TableT, NewTableT> table);

	<NewTableT extends Table> BindingContextFactoryBuilder<NewTableT> addSubEntity(String path, Function<TableT, NewTableT> table, BiFunction<TableT, NewTableT, Condition> bindCondition);


}
