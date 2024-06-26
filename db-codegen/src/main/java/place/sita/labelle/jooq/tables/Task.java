/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row5;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import place.sita.labelle.jooq.Keys;
import place.sita.labelle.jooq.Public;
import place.sita.labelle.jooq.enums.TaskStatus;
import place.sita.labelle.jooq.tables.records.TaskRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Task extends TableImpl<TaskRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.task</code>
     */
    public static final Task TASK = new Task();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TaskRecord> getRecordType() {
        return TaskRecord.class;
    }

    /**
     * The column <code>public.task.id</code>.
     */
    public final TableField<TaskRecord, UUID> ID = createField(DSL.name("id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.task.task_type_id</code>.
     */
    public final TableField<TaskRecord, UUID> TASK_TYPE_ID = createField(DSL.name("task_type_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.task.creation_date</code>.
     */
    public final TableField<TaskRecord, LocalDateTime> CREATION_DATE = createField(DSL.name("creation_date"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.task.status</code>.
     */
    public final TableField<TaskRecord, TaskStatus> STATUS = createField(DSL.name("status"), SQLDataType.VARCHAR.asEnumDataType(place.sita.labelle.jooq.enums.TaskStatus.class), this, "");

    /**
     * The column <code>public.task.parent</code>.
     */
    public final TableField<TaskRecord, UUID> PARENT = createField(DSL.name("parent"), SQLDataType.UUID.nullable(false), this, "");

    private Task(Name alias, Table<TaskRecord> aliased) {
        this(alias, aliased, null);
    }

    private Task(Name alias, Table<TaskRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.task</code> table reference
     */
    public Task(String alias) {
        this(DSL.name(alias), TASK);
    }

    /**
     * Create an aliased <code>public.task</code> table reference
     */
    public Task(Name alias) {
        this(alias, TASK);
    }

    /**
     * Create a <code>public.task</code> table reference
     */
    public Task() {
        this(DSL.name("task"), null);
    }

    public <O extends Record> Task(Table<O> child, ForeignKey<O, TaskRecord> key) {
        super(child, key, TASK);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<TaskRecord> getPrimaryKey() {
        return Keys.TASK_PKEY;
    }

    @Override
    public List<ForeignKey<TaskRecord, ?>> getReferences() {
        return Arrays.asList(Keys.TASK__FK_TASK_TASK_TYPE_ID);
    }

    private transient TaskType _taskType;

    public TaskType taskType() {
        if (_taskType == null)
            _taskType = new TaskType(this, Keys.TASK__FK_TASK_TASK_TYPE_ID);

        return _taskType;
    }

    @Override
    public Task as(String alias) {
        return new Task(DSL.name(alias), this);
    }

    @Override
    public Task as(Name alias) {
        return new Task(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Task rename(String name) {
        return new Task(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Task rename(Name name) {
        return new Task(name, null);
    }

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row5<UUID, UUID, LocalDateTime, TaskStatus, UUID> fieldsRow() {
        return (Row5) super.fieldsRow();
    }
}
