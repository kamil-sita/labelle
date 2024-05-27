package place.sita.labelle.core.repository.inrepository.delta;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.repository.inrepository.image.ImageRepository;
import place.sita.labelle.core.repository.inrepository.image.ImageResponse;
import place.sita.labelle.jooq.tables.ImageDelta;
import place.sita.labelle.jooq.tables.TagDelta;

import java.util.*;

@Component
public class DeltaRepository {

	private final DSLContext dslContext;
	private final ImageRepository imageRepository;

	public DeltaRepository(DSLContext dslContext, ImageRepository imageRepository) {
		this.dslContext = dslContext;
		this.imageRepository = imageRepository;
	}

	public List<TagDeltaResponse> getTagDeltas(UUID imageId) {
		return dslContext.select(TagDelta.TAG_DELTA.ADDS, TagDelta.TAG_DELTA.TAG, TagDelta.TAG_DELTA.FAMILY)
			.from(TagDelta.TAG_DELTA)
			.where(TagDelta.TAG_DELTA.IMAGE_ID.eq(imageId))
			.fetch()
			.map(rr -> {
				return new TagDeltaResponse(rr.value3(), rr.value2(), rr.value1() ? TagDeltaType.ADD : TagDeltaType.REMOVE);
			});
	}

	public Optional<ImageResponse> getImageDelta(UUID imageId) {
		return dslContext.select(ImageDelta.IMAGE_DELTA.IMAGE_RESOLVABLE_ID)
			.from(ImageDelta.IMAGE_DELTA)
			.where(ImageDelta.IMAGE_DELTA.IMAGE_ID.eq(imageId))
			.fetchOptional()
			.map(rr -> {
				return imageRepository.loadImage(rr.value1()).get();
			});
	}
}
