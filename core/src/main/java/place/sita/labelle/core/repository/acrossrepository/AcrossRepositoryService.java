package place.sita.labelle.core.repository.acrossrepository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.jooq.Tables;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AcrossRepositoryService {

	private final DSLContext dslContext;

	public AcrossRepositoryService(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<PullableImageResponse> getImages(UUID repositoryId, int offset, int limit, String search) {
		return dslContext
				.select(Tables.IMAGE.ID, Tables.IMAGE.REFERENCE_ID)
				.from(Tables.IMAGE)
				.where(Tables.IMAGE.REPOSITORY_ID.eq(repositoryId).and(Tables.IMAGE.VISIBLE_TO_CHILDREN.eq(true)))
				.orderBy(Tables.IMAGE.ID.desc())
				.limit(limit)
				.offset(offset)
				.fetch()
				.map(record -> new PullableImageResponse(record.get(Tables.IMAGE.ID), record.get(Tables.IMAGE.REFERENCE_ID)));
	}

	public Map<UUID, List<Tag>> getTags(List<UUID> images) {
		// todo in AcrossRepositoryService, we maybe should consider limiting it to publicly available images?
		return dslContext
				.select(Tables.IMAGE_TAGS.IMAGE_ID, Tables.IMAGE_TAGS.TAG_CATEGORY, Tables.IMAGE_TAGS.TAG)
				.from(Tables.IMAGE_TAGS)
				.where(Tables.IMAGE_TAGS.IMAGE_ID.in(images))
				.fetchGroups(Tables.IMAGE_TAGS.IMAGE_ID, record -> new Tag(record.get(Tables.IMAGE_TAGS.TAG_CATEGORY), record.get(Tables.IMAGE_TAGS.TAG)));
	}
}
