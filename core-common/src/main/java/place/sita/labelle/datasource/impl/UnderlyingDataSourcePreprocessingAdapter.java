package place.sita.labelle.datasource.impl;

import place.sita.labelle.datasource.Page;

public interface UnderlyingDataSourcePreprocessingAdapter<AcceptedPreprocessingType> {

	AcceptedPreprocessingType getPage(Page page);

}
