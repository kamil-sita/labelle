package place.sita.labelle.core.repository.acrossrepository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.jooq.Tables;

import java.util.Set;
import java.util.UUID;

@Service
public class UpdateService {

	private final DSLContext dslContext;

	public UpdateService(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Transactional
	public void update(UUID repoId, boolean runDelta, boolean runTagTranslation) {
		Set<UUID> parentRepos = dslContext.select(Tables.REPOSITORY_RELATIONSHIP.PARENT_ID)
			.from(Tables.REPOSITORY_RELATIONSHIP)
			.where(Tables.REPOSITORY_RELATIONSHIP.CHILD_ID.eq(repoId))
			.fetchSet(Tables.REPOSITORY_RELATIONSHIP.PARENT_ID);

		if (parentRepos.isEmpty()) {
			return;
		}

		var childImage = Tables.IMAGE.as("childImage");
		var parentImage = Tables.IMAGE.as("parentImage");

		Set<String> allPersistentParentIdsThatAreNotPresentInChildRepo = dslContext
			.select(parentImage.REFERENCE_ID)
			.from(parentImage)
			.where(parentImage.REPOSITORY_ID.in(parentRepos))
			.andNotExists(
				dslContext.select(childImage.REFERENCE_ID)
					.from(childImage)
					.where(childImage.REPOSITORY_ID.eq(repoId))
					.and(childImage.REFERENCE_ID.eq(parentImage.REFERENCE_ID))
			)
			.fetchSet(parentImage.REFERENCE_ID);

		if (!allPersistentParentIdsThatAreNotPresentInChildRepo.isEmpty()) {
			fetchNewEntries(repoId, allPersistentParentIdsThatAreNotPresentInChildRepo);
		}

		fetchUpdatesToTags(repoId);

		if (runDelta) {
			doRunDelta(repoId);
		}

		if (runTagTranslation) {
			doRunTagTranslation(repoId);
		}

	}

	private void fetchNewEntries(UUID repoId, Set<String> allPersistentParentIdsThatAreNotPresentInChildRepo) {

	}

	private void fetchUpdatesToTags(UUID repoId) {

	}

	private void doRunTagTranslation(UUID repoId) {

	}

	private void doRunDelta(UUID repoId) {

	}


}
