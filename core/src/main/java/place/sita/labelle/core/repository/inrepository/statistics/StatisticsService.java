package place.sita.labelle.core.repository.inrepository.statistics;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import place.sita.labelle.core.repository.inrepository.tags.TagValue;
import place.sita.labelle.jooq.Tables;

import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.count;

@Service
public class StatisticsService {

	private final DSLContext dslContext;

	public StatisticsService(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<TagWithCountResponse> getTagCount(UUID repositoryId) {
		return dslContext.select(Tables.IMAGE_TAGS.TAG_FAMILY, Tables.IMAGE_TAGS.TAG_VALUE, count())
			.from(Tables.IMAGE_TAGS)
			.where(Tables.IMAGE_TAGS.REPOSITORY_ID.eq(repositoryId))
			.groupBy(Tables.IMAGE_TAGS.TAG_FAMILY, Tables.IMAGE_TAGS.TAG_VALUE)
			.orderBy(count().desc())
			.fetch()
			.map(record -> new TagWithCountResponse(new TagValue(record.value1(), record.value2()), record.value3()));
	}
}
