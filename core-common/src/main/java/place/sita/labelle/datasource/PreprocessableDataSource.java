package place.sita.labelle.datasource;

public interface PreprocessableDataSource<Type, Processor, Self extends PreprocessableDataSource<Type, Processor, Self>> extends DataSource<Type, Self> {

	Processor process();

}
