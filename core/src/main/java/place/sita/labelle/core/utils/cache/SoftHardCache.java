package place.sita.labelle.core.utils.cache;

import java.lang.ref.SoftReference;
import java.util.*;

public class SoftHardCache<KeyT, ValueT> implements SimpleCache<KeyT, ValueT> {

	private int hardCacheSize;
	private int softCacheSize;

	public SoftHardCache(int hardCacheSize, int softCacheSize) {
		this.hardCacheSize = hardCacheSize;
		this.softCacheSize = softCacheSize;
	}

	public SoftHardCache() {
		this(5, 50);
	}

	private final Queue<KeyT> hardCacheLru = new ArrayDeque<>();
	private final Queue<KeyT> softCacheLru = new ArrayDeque<>();
	private final Map<KeyT, ValueT> hardCache = new HashMap<>();
	private final Map<KeyT, SoftReference<ValueT>> softCache = new HashMap<>();

	public Optional<ValueT> getFromCache(KeyT key) {
		Objects.requireNonNull(key);
		// hard cache?
		if (hardCache.containsKey(key)) {
			ValueT value = hardCache.get(key);
			if (value == null) {
				throw new RuntimeException("For key " + key  + " observed a null value in cache");
			}
			putIntoCache(key, value);
			return Optional.of(value);
		}

		// soft cache?
		if (softCache.containsKey(key)) {
			SoftReference<ValueT> valueReference = softCache.get(key);
			ValueT value = valueReference.get();

			if (value != null) {
				putIntoCache(key, value);
				return Optional.of(value);
			} else {
				softCache.remove(key);
				softCacheLru.remove(key);
			}
		}

		return Optional.empty();
	}

	public void putIntoCache(KeyT key, ValueT value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value, "obj for key " + key + " is null");
		hardCacheLru.remove(key);
		hardCacheLru.add(key);
		softCache.remove(key);
		softCacheLru.remove(key);

		hardCache.put(key, value);
		ensureCachesSize();
	}

	@Override
	public void clear() {
		hardCache.clear();
		softCache.clear();
		hardCacheLru.clear();
		softCacheLru.clear();
	}

	private void ensureCachesSize() {
		while (hardCacheLru.size() > hardCacheSize) {
			KeyT ptr = hardCacheLru.remove();
			ValueT type = hardCache.get(ptr);
			hardCache.remove(ptr);
			softCacheLru.add(ptr);
			softCache.put(ptr, new SoftReference<>(type));
		}

		while (softCacheLru.size() > softCacheSize) {
			KeyT ptr = softCacheLru.remove();
			SoftReference<ValueT> ref = softCache.get(ptr);
			softCache.remove(ptr);
			ref.clear();
		}
	}

	public SimpleCache<KeyT, ValueT> threadSafe() {
		Object sync = new Object();
		return new SimpleCache<>() {
			@Override
			public Optional<ValueT> getFromCache(KeyT key) {
				synchronized (sync) {
					return SoftHardCache.this.getFromCache(key);
				}
			}

			@Override
			public void putIntoCache(KeyT key, ValueT value) {
				synchronized (sync) {
					SoftHardCache.this.putIntoCache(key, value);
				}
			}

			@Override
			public void clear() {
				synchronized (sync) {
					SoftHardCache.this.clear();
				}
			}
		};
	}

}
