package place.sita.labelle.datasource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IdDataSource<Id, Type extends Identifiable<Id>, Self extends IdDataSource<Id, Type, Self>> extends DataSource<Type, Self> {

	default Type getById(Id id) {
		return getByIdOptional(id).orElse(null);
	}

	default Optional<Type> getByIdOptional(Id id) {
		return byId(id).getOneOptional();
	}

	default Self byId(Id id) {
		return byIds(List.of(id));
	}

	Self byIds(Collection<Id> ids);

	int indexOf(Type type);

}
