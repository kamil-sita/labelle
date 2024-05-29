package place.sita.labelle.datasource;

public interface DataSourceWithRemoval<T, Self extends DataSource<T, Self>> extends DataSource<T, Self> {

	void remove();

}
