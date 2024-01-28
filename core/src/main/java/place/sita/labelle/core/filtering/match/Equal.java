package place.sita.labelle.core.filtering.match;

import place.sita.labelle.core.filtering.Ref;

public record Equal<T>(Ref<T> ref1, Ref<T> ref2) {
}
