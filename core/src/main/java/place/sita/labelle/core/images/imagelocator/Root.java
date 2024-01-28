package place.sita.labelle.core.images.imagelocator;

import java.util.UUID;

public record Root(UUID id, String directory) {

    @Override
    public String toString() {
        return directory + " (" + id + ")";
    }
}
