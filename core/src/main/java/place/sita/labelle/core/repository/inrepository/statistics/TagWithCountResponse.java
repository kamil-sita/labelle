package place.sita.labelle.core.repository.inrepository.statistics;

import place.sita.labelle.core.repository.inrepository.tags.TagValue;

public record TagWithCountResponse(TagValue tag, int count) {
}
