package place.sita.labelle.core.repository.repositories;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import place.sita.labelle.core.TestContainersTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static place.sita.labelle.jooq.Tables.REPOSITORY;

public class RepositoryServiceTest extends TestContainersTest {

    @Autowired
    private RepositoryService service;

    @Autowired
    private DSLContext context;

    @AfterEach
    public void cleanup() {
        context.delete(REPOSITORY).execute();
    }

    @Test
    public void shouldByDefaultListNoRepositories() {
        // given
        // when
        var results = service.getRepositories();

        // then
        assertThat(results).isEmpty();
    }

    @Test
    public void shouldAddNewRepository() {
        // given

        // when
        service.addRepository("My test repo");

        // then
        var results = service.getRepositories();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("My test repo");
        assertThat(results.get(0).id()).isNotNull();
    }

    @Test
    public void shouldAddMultipleRepositories() {
        // given

        // when
        service.addRepository("My test repo");
        service.addRepository("My test repo 2");

        // then
        var results = service.getRepositories();
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("My test repo");
        assertThat(results.get(0).id()).isNotNull();
        assertThat(results.get(1).name()).isEqualTo("My test repo 2");
        assertThat(results.get(1).id()).isNotNull();
    }

    @Test
    public void shouldAddRepositoryWithKnownID() {
        // given

        // when
        service.addRepository(UUID.fromString("00000000-0000-0000-0000-000000000001"), "My test repo");

        // then
        var results = service.getRepositories();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("My test repo");
        assertThat(results.get(0).id()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

}
