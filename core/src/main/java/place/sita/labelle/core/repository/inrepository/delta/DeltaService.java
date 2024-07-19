package place.sita.labelle.core.repository.inrepository.delta;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.jooq.tables.TagDelta;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static place.sita.labelle.jooq.Tables.TAG_DELTA;
import static place.sita.labelle.jooq.Tables.TAG_DELTA_CALC;

@Service
public class DeltaService {

	private final DSLContext dslContext;

	public DeltaService(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Transactional
	public void addTagDelta(UUID imageId, String tag, String category, TagDeltaType added) {
		// does tag delta for this image id, tag and category already exist?
		dslContext.deleteFrom(TAG_DELTA)
			.where(TAG_DELTA.IMAGE_ID.eq(imageId)
				.and(TAG_DELTA.TAG.eq(tag))
				.and(TAG_DELTA.CATEGORY.eq(category))
			).execute();

		dslContext.insertInto(TagDelta.TAG_DELTA)
			.set(TagDelta.TAG_DELTA.IMAGE_ID, imageId)
			.set(TagDelta.TAG_DELTA.TAG, tag)
			.set(TagDelta.TAG_DELTA.CATEGORY, category)
			.set(TagDelta.TAG_DELTA.ADDS, added == TagDeltaType.ADD)
			.execute();
	}

	@Transactional
	public void recalculateTagDeltas(Set<UUID> imageIds) {
		dslContext.deleteFrom(TagDelta.TAG_DELTA)
			.where(TagDelta.TAG_DELTA.IMAGE_ID.in(imageIds))
			.execute();

		record DeltaTagView(boolean added, String tag, String category, UUID imageId) {}

		List<DeltaTagView> tagDeltas = dslContext.select(TAG_DELTA_CALC.ADDED, TAG_DELTA_CALC.TAG, TAG_DELTA_CALC.CATEGORY, TAG_DELTA_CALC.IMAGE_ID)
			.from(TAG_DELTA_CALC)
			.where(TAG_DELTA_CALC.IMAGE_ID.in(imageIds))
			.fetch()
			.map(rr -> {
				return new DeltaTagView(rr.value1(), rr.value2(), rr.value3(), rr.value4());
			});

		dslContext.batch(
			tagDeltas.stream()
				.map(delta -> {
					return dslContext.insertInto(TagDelta.TAG_DELTA)
						.set(TagDelta.TAG_DELTA.IMAGE_ID, delta.imageId)
						.set(TagDelta.TAG_DELTA.ADDS, delta.added)
						.set(TagDelta.TAG_DELTA.TAG, delta.tag)
						.set(TagDelta.TAG_DELTA.CATEGORY, delta.category);
				})
				.collect(Collectors.toList())
		).execute();
	}
}
