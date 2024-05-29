package place.sita.labelle.datasource.cross;

import place.sita.labelle.datasource.DataSourceWithRemoval;
import place.sita.labelle.datasource.PreprocessableDataSource;

public interface PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self extends DataSourceWithRemoval<T, Self> & PreprocessableDataSource<T, ProcessorApi, Self> & PreprocessableDataSourceWithRemoval<T, ProcessorApi, Self>>
	extends DataSourceWithRemoval<T, Self>,
	PreprocessableDataSource<T, ProcessorApi, Self> {
}
