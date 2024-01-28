package place.sita.labelle.core.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AffiliationMap {

    public static <IdT, ElementT> Map<IdT, ElementT> unique(Collection<ElementT> elements, Function<ElementT, IdT> extractor) {
        Map<IdT, ElementT> map = new HashMap<>();

        for (var element : elements) {
            IdT id = extractor.apply(element);
            if (map.containsKey(id)) {
                throw new RuntimeException("Duplicate key " + id);
            }
            map.put(id, element);
        }

        return map;
    }

}
