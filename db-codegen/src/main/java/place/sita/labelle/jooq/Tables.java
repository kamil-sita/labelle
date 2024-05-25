/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq;


import place.sita.labelle.jooq.tables.EffectiveHistoricConfiguration;
import place.sita.labelle.jooq.tables.Image;
import place.sita.labelle.jooq.tables.ImageDelta;
import place.sita.labelle.jooq.tables.ImageFile;
import place.sita.labelle.jooq.tables.ImageResolvable;
import place.sita.labelle.jooq.tables.ImageTags;
import place.sita.labelle.jooq.tables.Marker;
import place.sita.labelle.jooq.tables.Preferences;
import place.sita.labelle.jooq.tables.Repository;
import place.sita.labelle.jooq.tables.RepositoryRelationship;
import place.sita.labelle.jooq.tables.Root;
import place.sita.labelle.jooq.tables.Tag;
import place.sita.labelle.jooq.tables.TagDelta;
import place.sita.labelle.jooq.tables.TagImage;
import place.sita.labelle.jooq.tables.TagSrc;
import place.sita.labelle.jooq.tables.Task;
import place.sita.labelle.jooq.tables.TaskConfig;
import place.sita.labelle.jooq.tables.TaskDependencies;
import place.sita.labelle.jooq.tables.TaskExecution;
import place.sita.labelle.jooq.tables.TaskPlanning;
import place.sita.labelle.jooq.tables.TaskType;


/**
 * Convenience access to all tables in public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.effective_historic_configuration</code>.
     */
    public static final EffectiveHistoricConfiguration EFFECTIVE_HISTORIC_CONFIGURATION = EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION;

    /**
     * The table <code>public.image</code>.
     */
    public static final Image IMAGE = Image.IMAGE;

    /**
     * The table <code>public.image_delta</code>.
     */
    public static final ImageDelta IMAGE_DELTA = ImageDelta.IMAGE_DELTA;

    /**
     * The table <code>public.image_file</code>.
     */
    public static final ImageFile IMAGE_FILE = ImageFile.IMAGE_FILE;

    /**
     * The table <code>public.image_resolvable</code>.
     */
    public static final ImageResolvable IMAGE_RESOLVABLE = ImageResolvable.IMAGE_RESOLVABLE;

    /**
     * The table <code>public.image_tags</code>.
     */
    public static final ImageTags IMAGE_TAGS = ImageTags.IMAGE_TAGS;

    /**
     * The table <code>public.marker</code>.
     */
    public static final Marker MARKER = Marker.MARKER;

    /**
     * The table <code>public.preferences</code>.
     */
    public static final Preferences PREFERENCES = Preferences.PREFERENCES;

    /**
     * The table <code>public.repository</code>.
     */
    public static final Repository REPOSITORY = Repository.REPOSITORY;

    /**
     * The table <code>public.repository_relationship</code>.
     */
    public static final RepositoryRelationship REPOSITORY_RELATIONSHIP = RepositoryRelationship.REPOSITORY_RELATIONSHIP;

    /**
     * The table <code>public.root</code>.
     */
    public static final Root ROOT = Root.ROOT;

    /**
     * The table <code>public.tag</code>.
     */
    public static final Tag TAG = Tag.TAG;

    /**
     * The table <code>public.tag_delta</code>.
     */
    public static final TagDelta TAG_DELTA = TagDelta.TAG_DELTA;

    /**
     * The table <code>public.tag_image</code>.
     */
    public static final TagImage TAG_IMAGE = TagImage.TAG_IMAGE;

    /**
     * The table <code>public.tag_src</code>.
     */
    public static final TagSrc TAG_SRC = TagSrc.TAG_SRC;

    /**
     * The table <code>public.task</code>.
     */
    public static final Task TASK = Task.TASK;

    /**
     * The table <code>public.task_config</code>.
     */
    public static final TaskConfig TASK_CONFIG = TaskConfig.TASK_CONFIG;

    /**
     * The table <code>public.task_dependencies</code>.
     */
    public static final TaskDependencies TASK_DEPENDENCIES = TaskDependencies.TASK_DEPENDENCIES;

    /**
     * The table <code>public.task_execution</code>.
     */
    public static final TaskExecution TASK_EXECUTION = TaskExecution.TASK_EXECUTION;

    /**
     * The table <code>public.task_planning</code>.
     */
    public static final TaskPlanning TASK_PLANNING = TaskPlanning.TASK_PLANNING;

    /**
     * The table <code>public.task_type</code>.
     */
    public static final TaskType TASK_TYPE = TaskType.TASK_TYPE;
}
