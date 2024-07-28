package place.sita.labelle.core.repository.inrepository.delta;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;
import place.sita.labelle.core.repository.inrepository.InRepositoryService;
import place.sita.labelle.core.repository.inrepository.tags.Tag;
import place.sita.labelle.core.repository.inrepository.tags.TagRepository;
import place.sita.labelle.core.repository.repositories.Repository;
import place.sita.labelle.core.repository.repositories.RepositoryService;
import place.sita.labelle.jooq.Tables;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DeltaServiceTest extends TestContainersTest {

	@Autowired
	private DSLContext context;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private InRepositoryService inRepositoryService;

	@Autowired
	private DeltaService deltaService;

	@AfterEach
	public void cleanup() {
		context.delete(Tables.TAG_DELTA).execute();
		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_CATEGORY).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY_RELATIONSHIP).execute();
		context.delete(Tables.REPOSITORY).execute();
	}

	@Test
	public void shouldHaveEmptyDeltaIfHasNoTagsAndNoParents() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.recalculateTagDeltas(Set.of(imageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).isEmpty();
	}

	@Test
	public void shouldHaveEmptyDeltaIfHasNoTagsAndNeitherDoesOnlyParent() {
		// given
		Repository childRepo = repositoryService.addRepository("Child repo");
		Repository parentRepo = repositoryService.addRepository("Parent repo");
		repositoryService.addParentChild(childRepo.id(), parentRepo.id());

		UUID childImageId = inRepositoryService.images().addEmptySyntheticImage(childRepo.id());
		UUID parentImageId = inRepositoryService.images().addEmptySyntheticImage(parentRepo.id());

		inRepositoryService.images().setPersistentId(childImageId, "child persistent id");
		inRepositoryService.images().setPersistentId(parentImageId, "parent persistent id");
		inRepositoryService.images().setParentPersistentId(childImageId, "parent persistent id");

		// when
		deltaService.recalculateTagDeltas(Set.of(childImageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(childImageId);
		assertThat(tagDeltas).isEmpty();
	}

	@Test
	public void shouldHaveEmptyDeltaIfHasNoTagsAndNeitherDoBothParents() {
		// given
		Repository childRepo = repositoryService.addRepository("Child repo");
		Repository parentRepo1 = repositoryService.addRepository("Parent repo 1");
		Repository parentRepo2 = repositoryService.addRepository("Parent repo 2");
		repositoryService.addParentChild(childRepo.id(), parentRepo1.id());
		repositoryService.addParentChild(childRepo.id(), parentRepo2.id());

		UUID childImageId = inRepositoryService.images().addEmptySyntheticImage(childRepo.id());
		UUID parentImageId1 = inRepositoryService.images().addEmptySyntheticImage(parentRepo1.id());
		UUID parentImageId2 = inRepositoryService.images().addEmptySyntheticImage(parentRepo2.id());

		inRepositoryService.images().setPersistentId(childImageId, "child persistent id");
		inRepositoryService.images().setPersistentId(parentImageId1, "parent persistent id");
		inRepositoryService.images().setPersistentId(parentImageId2, "parent persistent id");
		inRepositoryService.images().setParentPersistentId(childImageId, "parent persistent id");

		// when
		deltaService.recalculateTagDeltas(Set.of(childImageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(childImageId);
		assertThat(tagDeltas).isEmpty();
	}

	@Test
	public void shouldNotCalculateDeltaIfHasTagsAndNoParentsReference() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));

		// when
		deltaService.recalculateTagDeltas(Set.of(imageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).isEmpty();
	}

	@Test
	public void shouldHaveDeltaIfHasTagsAndParentsReference() {
		// given
		Repository repo = repositoryService.addRepository("Test repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		inRepositoryService.images().setParentPersistentId(imageId, "parent persistent id");
		tagRepository.addTag(imageId, new Tag("Some category", "Some tag"));

		// when
		deltaService.recalculateTagDeltas(Set.of(imageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).hasSize(1);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.ADD);
	}

	@Test
	public void shouldHaveEmptyDeltaIfHasTagsAndParentAlsoHasThisTag() {
		// given
		Repository childRepo = repositoryService.addRepository("Child repo");
		Repository parentRepo = repositoryService.addRepository("Parent repo");
		repositoryService.addParentChild(childRepo.id(), parentRepo.id());

		UUID childImageId = inRepositoryService.images().addEmptySyntheticImage(childRepo.id());
		UUID parentImageId = inRepositoryService.images().addEmptySyntheticImage(parentRepo.id());

		inRepositoryService.images().setPersistentId(childImageId, "child persistent id");
		inRepositoryService.images().setPersistentId(parentImageId, "parent persistent id");
		inRepositoryService.images().setParentPersistentId(childImageId, "parent persistent id");

		tagRepository.addTag(childImageId, new Tag("Some category", "Some tag"));
		tagRepository.addTag(parentImageId, new Tag("Some category", "Some tag"));

		// when
		deltaService.recalculateTagDeltas(Set.of(childImageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(childImageId);
		assertThat(tagDeltas).isEmpty();
	}

	@Test
	public void shouldCalculateDeltaIfParentHasTagButThisDoesNot() {
		// given
		Repository childRepo = repositoryService.addRepository("Child repo");
		Repository parentRepo = repositoryService.addRepository("Parent repo");
		repositoryService.addParentChild(childRepo.id(), parentRepo.id());

		UUID childImageId = inRepositoryService.images().addEmptySyntheticImage(childRepo.id());
		UUID parentImageId = inRepositoryService.images().addEmptySyntheticImage(parentRepo.id());

		inRepositoryService.images().setPersistentId(childImageId, "child persistent id");
		inRepositoryService.images().setPersistentId(parentImageId, "parent persistent id");
		inRepositoryService.images().setParentPersistentId(childImageId, "parent persistent id");

		tagRepository.addTag(parentImageId, new Tag("Some category", "Some tag"));

		// when
		deltaService.recalculateTagDeltas(Set.of(childImageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(childImageId);
		assertThat(tagDeltas).hasSize(1);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.REMOVE);
	}

	@Test
	public void shouldHaveEmptyDeltaIfParentsHaveAllTags() {
		// given
		Repository childRepo = repositoryService.addRepository("Child repo");
		Repository parentRepo1 = repositoryService.addRepository("Parent repo 1");
		Repository parentRepo2 = repositoryService.addRepository("Parent repo 2");
		repositoryService.addParentChild(childRepo.id(), parentRepo1.id());
		repositoryService.addParentChild(childRepo.id(), parentRepo2.id());

		UUID childImageId = inRepositoryService.images().addEmptySyntheticImage(childRepo.id());
		UUID parentImageId1 = inRepositoryService.images().addEmptySyntheticImage(parentRepo1.id());
		UUID parentImageId2 = inRepositoryService.images().addEmptySyntheticImage(parentRepo2.id());

		inRepositoryService.images().setPersistentId(childImageId, "child persistent id");
		inRepositoryService.images().setPersistentId(parentImageId1, "parent persistent id");
		inRepositoryService.images().setPersistentId(parentImageId2, "parent persistent id");
		inRepositoryService.images().setParentPersistentId(childImageId, "parent persistent id");

		tagRepository.addTag(childImageId, new Tag("Some category", "Some tag 1"));
		tagRepository.addTag(childImageId, new Tag("Some category", "Some tag 2"));
		tagRepository.addTag(parentImageId1, new Tag("Some category", "Some tag 2"));
		tagRepository.addTag(parentImageId2, new Tag("Some category", "Some tag 1"));

		// when
		deltaService.recalculateTagDeltas(Set.of(childImageId));

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(childImageId);
		assertThat(tagDeltas).isEmpty();
	}

	@Test
	public void shouldManuallyAddPositiveDelta() {
		// given
		Repository repo = repositoryService.addRepository("Repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.ADD);

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).hasSize(1);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.ADD);
	}

	@Test
	public void shouldManuallyAddNegativeDelta() {
		// given
		Repository repo = repositoryService.addRepository("Repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.REMOVE);

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).hasSize(1);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.REMOVE);
	}

	@Test
	public void shouldManuallyAddManyDeltas() {
		// given
		Repository repo = repositoryService.addRepository("Repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.ADD);
		deltaService.addTagDelta(imageId, "Some tag 2", "Some category", TagDeltaType.REMOVE);

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).hasSize(2);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.ADD);
		assertThat(tagDeltas.get(1).tag()).isEqualTo("Some tag 2");
		assertThat(tagDeltas.get(1).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(1).type()).isEqualTo(TagDeltaType.REMOVE);
	}

	@Test
	public void shouldAddingNegativeDeltaRemovePositiveDelta() {
		// given
		Repository repo = repositoryService.addRepository("repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.ADD);
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.REMOVE);

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).hasSize(1);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.REMOVE);
	}

	@Test
	public void shouldAddingPositiveDeltaRemoveNegativeDelta() {
		// given
		Repository repo = repositoryService.addRepository("repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.REMOVE);
		deltaService.addTagDelta(imageId, "Some tag", "Some category", TagDeltaType.ADD);

		// then
		var tagDeltas = inRepositoryService.getTagDeltas(imageId);
		assertThat(tagDeltas).hasSize(1);
		assertThat(tagDeltas.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas.get(0).type()).isEqualTo(TagDeltaType.ADD);
	}

	@Test
	public void shouldAddingAPositiveDeltaNotAccidentallyRemoveADeltaFromAnotherImage() {
		// given
		Repository repo = repositoryService.addRepository("Repo");
		UUID imageId1 = inRepositoryService.images().addEmptySyntheticImage(repo.id());
		UUID imageId2 = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.addTagDelta(imageId1, "Some tag", "Some category", TagDeltaType.ADD);
		deltaService.addTagDelta(imageId2, "Some tag", "Some category", TagDeltaType.REMOVE);

		// then
		var tagDeltas1 = inRepositoryService.getTagDeltas(imageId1);
		assertThat(tagDeltas1).hasSize(1);
		assertThat(tagDeltas1.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas1.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas1.get(0).type()).isEqualTo(TagDeltaType.ADD);

		var tagDeltas2 = inRepositoryService.getTagDeltas(imageId2);
		assertThat(tagDeltas2).hasSize(1);
		assertThat(tagDeltas2.get(0).tag()).isEqualTo("Some tag");
		assertThat(tagDeltas2.get(0).category()).isEqualTo("Some category");
		assertThat(tagDeltas2.get(0).type()).isEqualTo(TagDeltaType.REMOVE);
	}

	@Test
	public void shouldSetTagDeltaToEnabled() {
		// given
		Repository repo = repositoryService.addRepository("Repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.enableTagDelta(imageId, true);

		// then
		assertThat(deltaService.isTagDeltaEnabled(imageId)).isTrue();
	}

	@Test
	public void shouldSetTagDeltaToDisabled() {
		// given
		Repository repo = repositoryService.addRepository("Repo");
		UUID imageId = inRepositoryService.images().addEmptySyntheticImage(repo.id());

		// when
		deltaService.enableTagDelta(imageId, false);

		// then
		assertThat(deltaService.isTagDeltaEnabled(imageId)).isFalse();
	}
}
