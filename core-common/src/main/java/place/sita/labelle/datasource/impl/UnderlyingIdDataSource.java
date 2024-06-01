package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.Identifiable;

import java.util.List;

public interface UnderlyingIdDataSource<Id, Type extends Identifiable<Id>, AcceptedPreprocessingType> extends UnderlyingDataSource<Type, AcceptedPreprocessingType> {

	int indexOf(Type type, List<AcceptedPreprocessingType> context);

}
