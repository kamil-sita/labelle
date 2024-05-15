package place.sita.labelle.core.filtering.ref;

import place.sita.labelle.core.filtering.Ref;

public record Value<T>(T value) implements Ref<T> {
}
