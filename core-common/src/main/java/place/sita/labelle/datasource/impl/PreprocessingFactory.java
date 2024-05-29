package place.sita.labelle.datasource.impl;

public interface PreprocessingFactory<ProcessingApiType, AcceptedPreprocessingType, DataSourceApiType> {

	ProcessingApiType createExposeableApi(PreprocessingApiAdapter<AcceptedPreprocessingType, DataSourceApiType> adapter);

}
