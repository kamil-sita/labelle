package place.sita.labelle.datasource.impl.jooq;

import org.jooq.TableField;

public record TableFieldAndValue<T>(TableField<?, T> field, T value) {
}
