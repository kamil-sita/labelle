package place.sita.labelle.core.cache;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CacheRegistry {

	private final List<InvalidateableCache> caches = new ArrayList<>();

	public CacheRegistry(List<InvalidateableCache> caches) {
		this.caches.addAll(caches);
	}

	public void invalidate() {
		caches.forEach(InvalidateableCache::invalidate);
	}

}
