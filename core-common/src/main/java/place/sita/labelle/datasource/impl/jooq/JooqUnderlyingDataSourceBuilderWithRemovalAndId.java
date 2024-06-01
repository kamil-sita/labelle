package place.sita.labelle.datasource.impl.jooq;

import place.sita.labelle.datasource.Identifiable;

import java.util.Collection;
import java.util.List;

public interface JooqUnderlyingDataSourceBuilderWithRemovalAndId<Id, Type extends Identifiable<Id>, AcceptedProcessingType> extends JooqUnderlyingDataSourceBuilderWithRemoval<Type, AcceptedProcessingType> {

	Collection<TableFieldAndValue> deconstructByOrder(Type type, List<AcceptedProcessingType> preprocessing);

}
