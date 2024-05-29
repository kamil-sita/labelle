package place.sita.labelle.datasource;

import java.util.Collection;
import java.util.Optional;

public interface IdDataSource<Id, Type extends Identifiable<Type>, Self extends IdDataSource<Id, Type, Self>> extends DataSource<Type, Self> {

	Type getById(Id id);

	Optional<Type> getByIdOptional(Id id);

	Self preprocessById(Id id);

	Self preprocessByIds(Collection<Id> ids);

}
