package place.sita.labelle.datasource.impl.jooq;

import org.jooq.Table;
import java.util.List;

public non-sealed interface JooqUnderlyingDataSourceBuilderWithRemoval<Type, AcceptedProcessingType> extends JooqUnderlyingDataSourceQueryBuilder<Type, AcceptedProcessingType>{

	Table<?> from(List<AcceptedProcessingType> preprocessing);

}
