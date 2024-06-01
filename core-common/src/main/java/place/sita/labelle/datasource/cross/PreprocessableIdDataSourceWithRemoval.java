package place.sita.labelle.datasource.cross;

import place.sita.labelle.datasource.IdDataSource;
import place.sita.labelle.datasource.Identifiable;
import place.sita.labelle.datasource.PreprocessableDataSource;

public interface PreprocessableIdDataSourceWithRemoval<Id, Type extends Identifiable<Id>, ProcessorApi, Self extends PreprocessableIdDataSourceWithRemoval<Id, Type, ProcessorApi, Self> & PreprocessableDataSourceWithRemoval<Type, ProcessorApi, Self>>
	extends IdDataSource<Id, Type, Self>,
	PreprocessableDataSourceWithRemoval<Type, ProcessorApi, Self> {
}
