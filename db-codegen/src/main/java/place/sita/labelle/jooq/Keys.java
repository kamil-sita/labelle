/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq;


import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

import place.sita.labelle.jooq.tables.Image;
import place.sita.labelle.jooq.tables.ImageFile;
import place.sita.labelle.jooq.tables.ImageResolvable;
import place.sita.labelle.jooq.tables.Marker;
import place.sita.labelle.jooq.tables.Preferences;
import place.sita.labelle.jooq.tables.Repository;
import place.sita.labelle.jooq.tables.RepositoryRelationship;
import place.sita.labelle.jooq.tables.Root;
import place.sita.labelle.jooq.tables.Tag;
import place.sita.labelle.jooq.tables.TagImage;
import place.sita.labelle.jooq.tables.TagSrc;
import place.sita.labelle.jooq.tables.Task;
import place.sita.labelle.jooq.tables.TaskConfig;
import place.sita.labelle.jooq.tables.TaskDependencies;
import place.sita.labelle.jooq.tables.TaskExecution;
import place.sita.labelle.jooq.tables.TaskType;
import place.sita.labelle.jooq.tables.records.ImageFileRecord;
import place.sita.labelle.jooq.tables.records.ImageRecord;
import place.sita.labelle.jooq.tables.records.ImageResolvableRecord;
import place.sita.labelle.jooq.tables.records.MarkerRecord;
import place.sita.labelle.jooq.tables.records.PreferencesRecord;
import place.sita.labelle.jooq.tables.records.RepositoryRecord;
import place.sita.labelle.jooq.tables.records.RepositoryRelationshipRecord;
import place.sita.labelle.jooq.tables.records.RootRecord;
import place.sita.labelle.jooq.tables.records.TagImageRecord;
import place.sita.labelle.jooq.tables.records.TagRecord;
import place.sita.labelle.jooq.tables.records.TagSrcRecord;
import place.sita.labelle.jooq.tables.records.TaskConfigRecord;
import place.sita.labelle.jooq.tables.records.TaskDependenciesRecord;
import place.sita.labelle.jooq.tables.records.TaskExecutionRecord;
import place.sita.labelle.jooq.tables.records.TaskRecord;
import place.sita.labelle.jooq.tables.records.TaskTypeRecord;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ImageRecord> IMAGE_PKEY = Internal.createUniqueKey(Image.IMAGE, DSL.name("image_pkey"), new TableField[] { Image.IMAGE.ID }, true);
    public static final UniqueKey<ImageFileRecord> IMAGE_FILE_PKEY = Internal.createUniqueKey(ImageFile.IMAGE_FILE, DSL.name("image_file_pkey"), new TableField[] { ImageFile.IMAGE_FILE.ID }, true);
    public static final UniqueKey<ImageResolvableRecord> IMAGE_RESOLVABLE_PKEY = Internal.createUniqueKey(ImageResolvable.IMAGE_RESOLVABLE, DSL.name("image_resolvable_pkey"), new TableField[] { ImageResolvable.IMAGE_RESOLVABLE.ID }, true);
    public static final UniqueKey<PreferencesRecord> PREFERENCES_PKEY = Internal.createUniqueKey(Preferences.PREFERENCES, DSL.name("preferences_pkey"), new TableField[] { Preferences.PREFERENCES.NAME }, true);
    public static final UniqueKey<RepositoryRecord> CATALOGUE_PKEY = Internal.createUniqueKey(Repository.REPOSITORY, DSL.name("catalogue_pkey"), new TableField[] { Repository.REPOSITORY.ID }, true);
    public static final UniqueKey<RepositoryRelationshipRecord> CATALOGUE_RELATIONSHIP_PKEY = Internal.createUniqueKey(RepositoryRelationship.REPOSITORY_RELATIONSHIP, DSL.name("catalogue_relationship_pkey"), new TableField[] { RepositoryRelationship.REPOSITORY_RELATIONSHIP.CHILD_ID, RepositoryRelationship.REPOSITORY_RELATIONSHIP.PARENT_ID }, true);
    public static final UniqueKey<RootRecord> ROOT_PKEY = Internal.createUniqueKey(Root.ROOT, DSL.name("root_pkey"), new TableField[] { Root.ROOT.ID }, true);
    public static final UniqueKey<TagRecord> TAG_PKEY = Internal.createUniqueKey(Tag.TAG, DSL.name("tag_pkey"), new TableField[] { Tag.TAG.ID }, true);
    public static final UniqueKey<TagSrcRecord> TAG_SRC_PKEY = Internal.createUniqueKey(TagSrc.TAG_SRC, DSL.name("tag_src_pkey"), new TableField[] { TagSrc.TAG_SRC.ID }, true);
    public static final UniqueKey<TaskRecord> TASK_PKEY = Internal.createUniqueKey(Task.TASK, DSL.name("task_pkey"), new TableField[] { Task.TASK.ID }, true);
    public static final UniqueKey<TaskConfigRecord> TASK_CONFIG_PKEY = Internal.createUniqueKey(TaskConfig.TASK_CONFIG, DSL.name("task_config_pkey"), new TableField[] { TaskConfig.TASK_CONFIG.TASK_ID }, true);
    public static final UniqueKey<TaskExecutionRecord> TASK_RESULT_PKEY = Internal.createUniqueKey(TaskExecution.TASK_EXECUTION, DSL.name("task_result_pkey"), new TableField[] { TaskExecution.TASK_EXECUTION.ID }, true);
    public static final UniqueKey<TaskTypeRecord> TASK_TYPE_PKEY = Internal.createUniqueKey(TaskType.TASK_TYPE, DSL.name("task_type_pkey"), new TableField[] { TaskType.TASK_TYPE.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ImageRecord, ImageResolvableRecord> IMAGE__FK_IMAGE_IMAGE_RESOLVABLE_ID = Internal.createForeignKey(Image.IMAGE, DSL.name("fk_image_image_resolvable_id"), new TableField[] { Image.IMAGE.IMAGE_RESOLVABLE_ID }, Keys.IMAGE_RESOLVABLE_PKEY, new TableField[] { ImageResolvable.IMAGE_RESOLVABLE.ID }, true);
    public static final ForeignKey<ImageRecord, ImageRecord> IMAGE__FK_IMAGE_PARENT_ID = Internal.createForeignKey(Image.IMAGE, DSL.name("fk_image_parent_id"), new TableField[] { Image.IMAGE.PARENT_ID }, Keys.IMAGE_PKEY, new TableField[] { Image.IMAGE.ID }, true);
    public static final ForeignKey<ImageRecord, RepositoryRecord> IMAGE__FK_IMAGE_REPOSITORY_ID = Internal.createForeignKey(Image.IMAGE, DSL.name("fk_image_repository_id"), new TableField[] { Image.IMAGE.REPOSITORY_ID }, Keys.CATALOGUE_PKEY, new TableField[] { Repository.REPOSITORY.ID }, true);
    public static final ForeignKey<ImageFileRecord, RootRecord> IMAGE_FILE__FK_IMAGE_FILE_ROOT_ID = Internal.createForeignKey(ImageFile.IMAGE_FILE, DSL.name("fk_image_file_root_id"), new TableField[] { ImageFile.IMAGE_FILE.ROOT_ID }, Keys.ROOT_PKEY, new TableField[] { Root.ROOT.ID }, true);
    public static final ForeignKey<ImageResolvableRecord, ImageFileRecord> IMAGE_RESOLVABLE__FK_IMAGE_RESOLVABLE_IMAGE_FILE_ID = Internal.createForeignKey(ImageResolvable.IMAGE_RESOLVABLE, DSL.name("fk_image_resolvable_image_file_id"), new TableField[] { ImageResolvable.IMAGE_RESOLVABLE.IMAGE_FILE_ID }, Keys.IMAGE_FILE_PKEY, new TableField[] { ImageFile.IMAGE_FILE.ID }, true);
    public static final ForeignKey<MarkerRecord, ImageRecord> MARKER__MARKER_IMAGE_ID = Internal.createForeignKey(Marker.MARKER, DSL.name("marker_image_id"), new TableField[] { Marker.MARKER.IMAGE_ID }, Keys.IMAGE_PKEY, new TableField[] { Image.IMAGE.ID }, true);
    public static final ForeignKey<RepositoryRelationshipRecord, RepositoryRecord> REPOSITORY_RELATIONSHIP__FK_CATALOGUE_RELATIONSHIP_CHILD_ID = Internal.createForeignKey(RepositoryRelationship.REPOSITORY_RELATIONSHIP, DSL.name("fk_catalogue_relationship_child_id"), new TableField[] { RepositoryRelationship.REPOSITORY_RELATIONSHIP.CHILD_ID }, Keys.CATALOGUE_PKEY, new TableField[] { Repository.REPOSITORY.ID }, true);
    public static final ForeignKey<RepositoryRelationshipRecord, RepositoryRecord> REPOSITORY_RELATIONSHIP__FK_CATALOGUE_RELATIONSHIP_PARENT_ID = Internal.createForeignKey(RepositoryRelationship.REPOSITORY_RELATIONSHIP, DSL.name("fk_catalogue_relationship_parent_id"), new TableField[] { RepositoryRelationship.REPOSITORY_RELATIONSHIP.PARENT_ID }, Keys.CATALOGUE_PKEY, new TableField[] { Repository.REPOSITORY.ID }, true);
    public static final ForeignKey<TagRecord, TagSrcRecord> TAG__FK_TAG_TAG_SRC_ID_3b = Internal.createForeignKey(Tag.TAG, DSL.name("fk_tag_tag_src_id;"), new TableField[] { Tag.TAG.TAG_SRC_ID }, Keys.TAG_SRC_PKEY, new TableField[] { TagSrc.TAG_SRC.ID }, true);
    public static final ForeignKey<TagImageRecord, ImageRecord> TAG_IMAGE__FK_TAG_IMAGE_ID = Internal.createForeignKey(TagImage.TAG_IMAGE, DSL.name("fk_tag_image_id"), new TableField[] { TagImage.TAG_IMAGE.IMAGE_ID }, Keys.IMAGE_PKEY, new TableField[] { Image.IMAGE.ID }, true);
    public static final ForeignKey<TagImageRecord, TagRecord> TAG_IMAGE__FK_TAG_IMAGE_TAG_ID = Internal.createForeignKey(TagImage.TAG_IMAGE, DSL.name("fk_tag_image_tag_id"), new TableField[] { TagImage.TAG_IMAGE.TAG_ID }, Keys.TAG_PKEY, new TableField[] { Tag.TAG.ID }, true);
    public static final ForeignKey<TagSrcRecord, RepositoryRecord> TAG_SRC__FK_TAG_SRC_REPOSITORY_ID = Internal.createForeignKey(TagSrc.TAG_SRC, DSL.name("fk_tag_src_repository_id"), new TableField[] { TagSrc.TAG_SRC.REPOSITORY_ID }, Keys.CATALOGUE_PKEY, new TableField[] { Repository.REPOSITORY.ID }, true);
    public static final ForeignKey<TaskRecord, TaskTypeRecord> TASK__FK_TASK_TASK_TYPE_ID = Internal.createForeignKey(Task.TASK, DSL.name("fk_task_task_type_id"), new TableField[] { Task.TASK.TASK_TYPE_ID }, Keys.TASK_TYPE_PKEY, new TableField[] { TaskType.TASK_TYPE.ID }, true);
    public static final ForeignKey<TaskConfigRecord, TaskRecord> TASK_CONFIG__FK_TASK_CONFIG_TASK_ID = Internal.createForeignKey(TaskConfig.TASK_CONFIG, DSL.name("fk_task_config_task_id"), new TableField[] { TaskConfig.TASK_CONFIG.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<TaskDependenciesRecord, TaskRecord> TASK_DEPENDENCIES__TASK_DEPENDENCIES_TASK_DEPENDENCY_ID = Internal.createForeignKey(TaskDependencies.TASK_DEPENDENCIES, DSL.name("tasK_dependencies_task_dependency_id"), new TableField[] { TaskDependencies.TASK_DEPENDENCIES.REQUIRED_DEPENDENCY_TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<TaskDependenciesRecord, TaskRecord> TASK_DEPENDENCIES__TASK_DEPENDENCIES_TASK_ID = Internal.createForeignKey(TaskDependencies.TASK_DEPENDENCIES, DSL.name("tasK_dependencies_task_id"), new TableField[] { TaskDependencies.TASK_DEPENDENCIES.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
    public static final ForeignKey<TaskExecutionRecord, TaskRecord> TASK_EXECUTION__FK_TASK_RESULT_TASK_ID = Internal.createForeignKey(TaskExecution.TASK_EXECUTION, DSL.name("fk_task_result_task_id"), new TableField[] { TaskExecution.TASK_EXECUTION.TASK_ID }, Keys.TASK_PKEY, new TableField[] { Task.TASK.ID }, true);
}
