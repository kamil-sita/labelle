package place.sita.labelle.core.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import place.sita.labelle.core.cache.InvalidateableCache;
import place.sita.labelle.core.persistence.ex.IllegalOperationOnStateException;
import place.sita.labelle.core.persistence.ex.UnexpectedDatabaseReplyException;
import place.sita.labelle.core.images.imagelocator.Root;
import place.sita.labelle.core.utils.Result2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static place.sita.labelle.jooq.tables.Root.ROOT;

@Repository
public class RootRepository implements InvalidateableCache {

    private final DSLContext dslContext;

    private List<Root> rootCache = null;

    public RootRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Transactional(readOnly = true)
    public List<Root> getRoots() {
        if (rootCache != null) {
            return rootCache;
        }
        List<Root> roots = dslContext.select(ROOT.ID, ROOT.ROOT_DIR)
            .from(ROOT)
            .fetch()
            .map(rr -> {
                return new Root(rr.value1(), rr.value2());
            });
        rootCache = Collections.synchronizedList(new ArrayList<>(roots));
        return roots;
    }

    @Transactional
    public Result2<Root, BaseOfRootViolation> createRoot(String text) {
        List<Root> roots = getRoots();

        for (var root : roots) {
            if (root.directory().startsWith(text)) {
                return Result2.failure(new BaseOfRootViolation());
            }
            if (text.startsWith(root.directory())) {
                return Result2.failure(new BaseOfRootViolation());
            }
        }

        UUID uuid = UUID.randomUUID();
        int insert = dslContext.insertInto(ROOT, ROOT.ID, ROOT.ROOT_DIR)
            .values(uuid, text)
            .execute();
        if (insert != 1) {
            throw new UnexpectedDatabaseReplyException();
        }
        Root root = new Root(uuid, text);
        if (rootCache != null) {
            rootCache.add(root);
        }
        return Result2.success(root);
    }

    @Transactional
    public Result2<Void, RemovalNotPossibleDueToConstraints> remove(UUID id) {
        List<Root> roots = getRoots();
        if (!roots.stream().map(Root::id).collect(Collectors.toSet()).contains(id)) {
            throw new IllegalOperationOnStateException();
        }
        int delete = dslContext.deleteFrom(ROOT)
            .where(ROOT.ID.eq(id))
            .execute();
        if (delete == 0) {
            return Result2.failure(new RemovalNotPossibleDueToConstraints());
        }
        rootCache = Collections.synchronizedList(rootCache.stream()
            .filter(r -> !r.id().equals(id))
            .toList());
        return Result2.success(null);
    }

    @Override
    public void invalidate() {
        rootCache = null;
    }

    public record BaseOfRootViolation() {

    }

    public record RemovalNotPossibleDueToConstraints() {

    }
}
