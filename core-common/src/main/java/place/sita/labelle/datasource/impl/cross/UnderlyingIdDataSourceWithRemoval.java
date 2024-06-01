package place.sita.labelle.datasource.impl.cross;

import place.sita.labelle.datasource.Identifiable;
import place.sita.labelle.datasource.impl.UnderlyingDataSourceWithRemoval;
import place.sita.labelle.datasource.impl.UnderlyingIdDataSource;

public interface UnderlyingIdDataSourceWithRemoval<Id, Type extends Identifiable<Id>, AcceptedPreprocessingType> extends UnderlyingIdDataSource<Id, Type, AcceptedPreprocessingType>, UnderlyingDataSourceWithRemoval<Type, AcceptedPreprocessingType> {
}
