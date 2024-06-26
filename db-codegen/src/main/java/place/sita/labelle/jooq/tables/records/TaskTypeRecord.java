/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

import place.sita.labelle.jooq.tables.TaskType;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TaskTypeRecord extends UpdatableRecordImpl<TaskTypeRecord> implements Record3<UUID, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.task_type.id</code>.
     */
    public void setId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.task_type.id</code>.
     */
    public UUID getId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.task_type.code</code>.
     */
    public void setCode(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.task_type.code</code>.
     */
    public String getCode() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.task_type.name</code>.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.task_type.name</code>.
     */
    public String getName() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<UUID, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<UUID, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return TaskType.TASK_TYPE.ID;
    }

    @Override
    public Field<String> field2() {
        return TaskType.TASK_TYPE.CODE;
    }

    @Override
    public Field<String> field3() {
        return TaskType.TASK_TYPE.NAME;
    }

    @Override
    public UUID component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getCode();
    }

    @Override
    public String component3() {
        return getName();
    }

    @Override
    public UUID value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getCode();
    }

    @Override
    public String value3() {
        return getName();
    }

    @Override
    public TaskTypeRecord value1(UUID value) {
        setId(value);
        return this;
    }

    @Override
    public TaskTypeRecord value2(String value) {
        setCode(value);
        return this;
    }

    @Override
    public TaskTypeRecord value3(String value) {
        setName(value);
        return this;
    }

    @Override
    public TaskTypeRecord values(UUID value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TaskTypeRecord
     */
    public TaskTypeRecord() {
        super(TaskType.TASK_TYPE);
    }

    /**
     * Create a detached, initialised TaskTypeRecord
     */
    public TaskTypeRecord(UUID id, String code, String name) {
        super(TaskType.TASK_TYPE);

        setId(id);
        setCode(code);
        setName(name);
    }
}
