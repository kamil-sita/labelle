package place.sita.labelle.datasource.impl.jooq;

import org.jooq.DSLContext;

public interface JooqContextProvider {
	DSLContext getContext();
}
