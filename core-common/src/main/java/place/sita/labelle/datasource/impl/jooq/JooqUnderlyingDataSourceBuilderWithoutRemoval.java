package place.sita.labelle.datasource.impl.jooq;

import org.jooq.TableLike;

import java.util.Collection;
import java.util.List;

public non-sealed interface JooqUnderlyingDataSourceBuilderWithoutRemoval<Type, AcceptedProcessingType> extends JooqUnderlyingDataSourceQueryBuilder<Type, AcceptedProcessingType>{

	Collection<? extends TableLike<?>> from(List<AcceptedProcessingType> preprocessing);
}
