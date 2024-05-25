package place.sita.labelle.core.repository.inrepository.image;

import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static place.sita.labelle.jooq.tables.Image.IMAGE;

@Component
public class ImageRepository {


	private final DSLContext dslContext;

	public ImageRepository(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	public List<ImageResponse> images(UUID repositoryUuid, int offset, int limit, String query) {
		return dslContext
			.select(IMAGE.ID, IMAGE.imageResolvable().imageFile().RELATIVE_DIR, IMAGE.imageResolvable().imageFile().root().ROOT_DIR)
			.from(IMAGE)
			.where(IMAGE.REPOSITORY_ID.equal(repositoryUuid))
			.orderBy(IMAGE.ID)
			.limit(limit)
			.offset(offset)
			.fetch()
			.map(rr -> {
				return new ImageResponse(rr.value1(), rr.value3(), rr.value2());
			});
	}

	public Optional<ImageResponse> loadImage(UUID imageId) {
		return dslContext
			.select(IMAGE.ID, IMAGE.imageResolvable().imageFile().RELATIVE_DIR, IMAGE.imageResolvable().imageFile().root().ROOT_DIR)
			.from(IMAGE)
			.where(IMAGE.ID.eq(imageId))
			.fetchOptional()
			.map(rr -> {
				return new ImageResponse(rr.value1(), rr.value3(), rr.value2());
			});
	}


}
