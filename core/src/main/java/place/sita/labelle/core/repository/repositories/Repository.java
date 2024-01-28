package place.sita.labelle.core.repository.repositories;

import java.util.UUID;

public record Repository(UUID id, String name) {

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
