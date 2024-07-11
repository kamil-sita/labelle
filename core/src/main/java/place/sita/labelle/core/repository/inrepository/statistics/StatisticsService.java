package place.sita.labelle.core.repository.inrepository.statistics;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import place.sita.labelle.core.repository.inrepository.tags.Tag;

import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.count;
import static place.sita.labelle.jooq.Tables.IMAGE_TAGS;

@Service
public class StatisticsService {

	private final DSLContext dslContext;

	public StatisticsService(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<TagWithCountResponse> getTagCount(UUID repositoryId) {
		return dslContext.select(IMAGE_TAGS.TAG_CATEGORY, IMAGE_TAGS.TAG, count())
			.from(IMAGE_TAGS)
			.where(IMAGE_TAGS.REPOSITORY_ID.eq(repositoryId))
			.groupBy(IMAGE_TAGS.TAG_CATEGORY, IMAGE_TAGS.TAG)
			.orderBy(count().desc())
			.fetch()
			.map(record -> new TagWithCountResponse(new Tag(record.value1(), record.value2()), record.value3()));
	}
}
