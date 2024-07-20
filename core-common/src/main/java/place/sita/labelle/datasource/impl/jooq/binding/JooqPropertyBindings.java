package place.sita.labelle.datasource.impl.jooq.binding;

import org.jooq.Field;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;

public class JooqPropertyBindings {

	private final Map<LogicalPath, Field> bindings = new HashMap<>();
	private final Map<LogicalPath, Table> tables = new HashMap<>();

	public void addBinding(LogicalPath path, Field binding) {
		bindings.put(path, binding);
	}

	public Field getBinding(LogicalPath path) {
		return bindings.get(path);
	}

	public void addTable(LogicalPath path, Table table) {
		tables.put(path, table);
	}

	public Table getTable(LogicalPath path) {
		return tables.get(path);
	}

}
