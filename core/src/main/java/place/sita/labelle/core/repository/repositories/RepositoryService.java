package place.sita.labelle.core.repository.repositories;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import org.springframework.stereotype.Component;
import place.sita.labelle.core.persistence.JqRepo;

import java.util.List;
import java.util.UUID;

import static place.sita.labelle.jooq.Tables.REPOSITORY;
import static place.sita.labelle.jooq.Tables.REPOSITORY_RELATIONSHIP;

@Component
public class RepositoryService {

    private final DSLContext context;

    public RepositoryService(DSLContext context) {
        this.context = context;
    }

    public List<Repository> getRepositories() {
        var results = context
            .select(REPOSITORY.ID, REPOSITORY.NAME)
            .from(REPOSITORY)
            .fetch()
            .map(toRepository())
            .stream()
            .toList();
        return results;
    }

    public List<Repository> getParents(UUID id) {
        var results = context
            .select(REPOSITORY.ID, REPOSITORY.NAME)
            .from(REPOSITORY, REPOSITORY_RELATIONSHIP)
            .where(REPOSITORY.ID.eq(REPOSITORY_RELATIONSHIP.PARENT_ID)
                .and(REPOSITORY_RELATIONSHIP.CHILD_ID.eq(id)))
            .fetch()
            .map(toRepository())
            .stream()
            .toList();
        return results;
    }

    public List<Repository> getChildren(UUID id) {
        var results = context
            .select(REPOSITORY.ID, REPOSITORY.NAME)
            .from(REPOSITORY, REPOSITORY_RELATIONSHIP)
            .where(REPOSITORY.ID.eq(REPOSITORY_RELATIONSHIP.CHILD_ID)
                .and(REPOSITORY_RELATIONSHIP.PARENT_ID.eq(id)))
            .fetch()
            .map(toRepository())
            .stream()
            .toList();
        return results;
    }

    private static RecordMapper<Record2<UUID, String>, Repository> toRepository() {
        return rr -> {
            return new Repository(
                rr.value1(),
                rr.value2()
            );
        };
    }

    public Repository addRepository(String name) {
        return addRepository(UUID.randomUUID(), name);
    }

    public Repository addRepository(UUID id, String name) {
        JqRepo.insertOne(() ->
            context.insertInto(REPOSITORY)
                .columns(REPOSITORY.ID, REPOSITORY.NAME)
                .values(id, name)
                .execute()
        );
        return new Repository(id, name);
    }

    public void deleteRepository(UUID id) {
        context
            .delete(REPOSITORY)
            .where(REPOSITORY.ID.eq(id))
            .execute();
    }

    public void addParentChild(UUID childId, UUID parentId) {
        context
            .insertInto(REPOSITORY_RELATIONSHIP)
            .columns(REPOSITORY_RELATIONSHIP.CHILD_ID, REPOSITORY_RELATIONSHIP.PARENT_ID)
            .values(childId, parentId)
            .execute();
    }

    public void removeParentChild(UUID childId, UUID parentId) {
        context
            .delete(REPOSITORY_RELATIONSHIP)
            .where(REPOSITORY_RELATIONSHIP.CHILD_ID.eq(childId).and(REPOSITORY_RELATIONSHIP.PARENT_ID.eq(parentId)))
            .execute();
    }

    public Repository rename(UUID id, String text) {
        int result = context
            .update(REPOSITORY)
            .set(REPOSITORY.NAME, text)
            .where(REPOSITORY.ID.equal(id))
            .execute();

        if (result == 1) {
            return new Repository(id, text);
        } else {
            throw new RuntimeException("Impossible to update");
        }
    }
}
