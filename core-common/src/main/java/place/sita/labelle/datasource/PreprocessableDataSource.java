package place.sita.labelle.datasource;

public interface PreprocessableDataSource<Type, FilteringType, SortingType, Self extends PreprocessableDataSource<Type, FilteringType, SortingType, Self>> extends DataSource<Type, Self> {

	Self processFiltering(FilteringType filter);

	Self processSorting(SortingType sort);

	Self process(FilteringType filter, SortingType sort);

}
