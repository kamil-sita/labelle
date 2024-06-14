package place.sita.labelle;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.framework.junit5.ApplicationExtension;
import place.sita.labelle.core.cache.CacheRegistry;
import place.sita.modulefx.UnstableSceneReporter;
import place.sita.labelle.jooq.Tables;

@ExtendWith(ApplicationExtension.class)
public abstract class GuiTest extends TestContainersTest {

	protected UnstableSceneReporter unstableSceneReporter;

	public void setUnstableSceneReporter(UnstableSceneReporter unstableSceneReporter) {
		this.unstableSceneReporter = unstableSceneReporter;
	}

	@Autowired
	private DSLContext context;

	@Autowired
	private CacheRegistry cacheRegistry;

	@AfterEach
	public void cleanup() {
		context.delete(Tables.TAG_DELTA).execute();

		context.delete(Tables.TAG_IMAGE).execute();
		context.delete(Tables.TAG).execute();
		context.delete(Tables.TAG_SRC).execute();

		context.delete(Tables.IMAGE).execute();
		context.delete(Tables.IMAGE_RESOLVABLE).execute();

		context.delete(Tables.REPOSITORY_RELATIONSHIP).execute();
		context.delete(Tables.REPOSITORY).execute();

		context.delete(Tables.IMAGE_FILE).execute();
		context.delete(Tables.ROOT).execute();
		cacheRegistry.invalidate();
	}

}
