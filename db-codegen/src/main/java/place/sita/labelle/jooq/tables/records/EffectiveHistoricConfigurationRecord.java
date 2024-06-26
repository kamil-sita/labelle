/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.TableRecordImpl;

import place.sita.labelle.jooq.enums.TaskExecutionResult;
import place.sita.labelle.jooq.tables.EffectiveHistoricConfiguration;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class EffectiveHistoricConfigurationRecord extends TableRecordImpl<EffectiveHistoricConfigurationRecord> implements Record7<UUID, UUID, String, TaskExecutionResult, OffsetDateTime, OffsetDateTime, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.effective_historic_configuration.id</code>.
     */
    public void setId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.effective_historic_configuration.id</code>.
     */
    public UUID getId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.effective_historic_configuration.task_id</code>.
     */
    public void setTaskId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.effective_historic_configuration.task_id</code>.
     */
    public UUID getTaskId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>public.effective_historic_configuration.log</code>.
     */
    public void setLog(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.effective_historic_configuration.log</code>.
     */
    public String getLog() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.effective_historic_configuration.result</code>.
     */
    public void setResult(TaskExecutionResult value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.effective_historic_configuration.result</code>.
     */
    public TaskExecutionResult getResult() {
        return (TaskExecutionResult) get(3);
    }

    /**
     * Setter for
     * <code>public.effective_historic_configuration.finished_at</code>.
     */
    public void setFinishedAt(OffsetDateTime value) {
        set(4, value);
    }

    /**
     * Getter for
     * <code>public.effective_historic_configuration.finished_at</code>.
     */
    public OffsetDateTime getFinishedAt() {
        return (OffsetDateTime) get(4);
    }

    /**
     * Setter for
     * <code>public.effective_historic_configuration.started_at</code>.
     */
    public void setStartedAt(OffsetDateTime value) {
        set(5, value);
    }

    /**
     * Getter for
     * <code>public.effective_historic_configuration.started_at</code>.
     */
    public OffsetDateTime getStartedAt() {
        return (OffsetDateTime) get(5);
    }

    /**
     * Setter for
     * <code>public.effective_historic_configuration.configuration</code>.
     */
    public void setConfiguration(String value) {
        set(6, value);
    }

    /**
     * Getter for
     * <code>public.effective_historic_configuration.configuration</code>.
     */
    public String getConfiguration() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<UUID, UUID, String, TaskExecutionResult, OffsetDateTime, OffsetDateTime, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<UUID, UUID, String, TaskExecutionResult, OffsetDateTime, OffsetDateTime, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.ID;
    }

    @Override
    public Field<UUID> field2() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.TASK_ID;
    }

    @Override
    public Field<String> field3() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.LOG;
    }

    @Override
    public Field<TaskExecutionResult> field4() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.RESULT;
    }

    @Override
    public Field<OffsetDateTime> field5() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.FINISHED_AT;
    }

    @Override
    public Field<OffsetDateTime> field6() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.STARTED_AT;
    }

    @Override
    public Field<String> field7() {
        return EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION.CONFIGURATION;
    }

    @Override
    public UUID component1() {
        return getId();
    }

    @Override
    public UUID component2() {
        return getTaskId();
    }

    @Override
    public String component3() {
        return getLog();
    }

    @Override
    public TaskExecutionResult component4() {
        return getResult();
    }

    @Override
    public OffsetDateTime component5() {
        return getFinishedAt();
    }

    @Override
    public OffsetDateTime component6() {
        return getStartedAt();
    }

    @Override
    public String component7() {
        return getConfiguration();
    }

    @Override
    public UUID value1() {
        return getId();
    }

    @Override
    public UUID value2() {
        return getTaskId();
    }

    @Override
    public String value3() {
        return getLog();
    }

    @Override
    public TaskExecutionResult value4() {
        return getResult();
    }

    @Override
    public OffsetDateTime value5() {
        return getFinishedAt();
    }

    @Override
    public OffsetDateTime value6() {
        return getStartedAt();
    }

    @Override
    public String value7() {
        return getConfiguration();
    }

    @Override
    public EffectiveHistoricConfigurationRecord value1(UUID value) {
        setId(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord value2(UUID value) {
        setTaskId(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord value3(String value) {
        setLog(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord value4(TaskExecutionResult value) {
        setResult(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord value5(OffsetDateTime value) {
        setFinishedAt(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord value6(OffsetDateTime value) {
        setStartedAt(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord value7(String value) {
        setConfiguration(value);
        return this;
    }

    @Override
    public EffectiveHistoricConfigurationRecord values(UUID value1, UUID value2, String value3, TaskExecutionResult value4, OffsetDateTime value5, OffsetDateTime value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached EffectiveHistoricConfigurationRecord
     */
    public EffectiveHistoricConfigurationRecord() {
        super(EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION);
    }

    /**
     * Create a detached, initialised EffectiveHistoricConfigurationRecord
     */
    public EffectiveHistoricConfigurationRecord(UUID id, UUID taskId, String log, TaskExecutionResult result, OffsetDateTime finishedAt, OffsetDateTime startedAt, String configuration) {
        super(EffectiveHistoricConfiguration.EFFECTIVE_HISTORIC_CONFIGURATION);

        setId(id);
        setTaskId(taskId);
        setLog(log);
        setResult(result);
        setFinishedAt(finishedAt);
        setStartedAt(startedAt);
        setConfiguration(configuration);
    }
}
