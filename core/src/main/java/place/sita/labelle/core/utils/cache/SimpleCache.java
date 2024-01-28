package place.sita.labelle.core.utils.cache;

import java.util.Optional;

public interface SimpleCache<KeyT, ValueT> {

	Optional<ValueT> getFromCache(KeyT key);

	void putIntoCache(KeyT key, ValueT value);

	void clear();
}
