package place.sita.labelle.datasource.impl.jooq;

import org.jooq.*;
import org.jooq.Record;

import java.util.Collection;
import java.util.List;

public sealed interface JooqUnderlyingDataSourceQueryBuilder<Type, AcceptedProcessingType>
	permits JooqUnderlyingDataSourceBuilderWithRemoval, JooqUnderlyingDataSourceBuilderWithoutRemoval {

	RecordMapper<Record, Type> mapper();

	Collection<? extends SelectFieldOrAsterisk> select(List<AcceptedProcessingType> preprocessing);

	Collection<? extends OrderField<?>> orderBy(List<AcceptedProcessingType> preprocessing);

	Collection<? extends Condition> where(List<AcceptedProcessingType> preprocessing);

	Number limit(List<AcceptedProcessingType> preprocessing);

	Number offset(List<AcceptedProcessingType> preprocessing);
}
