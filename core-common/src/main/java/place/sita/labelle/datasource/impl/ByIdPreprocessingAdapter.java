package place.sita.labelle.datasource.impl;

import java.util.Collection;

public interface ByIdPreprocessingAdapter<Id, AcceptedProcessingType> {

	AcceptedProcessingType accept(Collection<Id> ids);

}
