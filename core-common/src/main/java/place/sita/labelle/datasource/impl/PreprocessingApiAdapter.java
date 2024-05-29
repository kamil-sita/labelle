package place.sita.labelle.datasource.impl;

public interface PreprocessingApiAdapter<AcceptedPreprocessingType, ApiT> {

	ApiT accept(AcceptedPreprocessingType acceptedPreprocessingType);

}
