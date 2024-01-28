/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables;


import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row7;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import place.sita.labelle.jooq.Public;
import place.sita.labelle.jooq.enums.TaskExecutionResult;
import place.sita.labelle.jooq.tables.records.EffectiveHistoricConfigurationRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class EffectiveHistoricConfiguration extends TableImpl<EffectiveHistoricConfigurationRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of
     * <code>public.effective_historic_configuration</code>
     */
    public static final EffectiveHistoricConfiguration EFFECTIVE_HISTORIC_CONFIGURATION = new EffectiveHistoricConfiguration();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<EffectiveHistoricConfigurationRecord> getRecordType() {
        return EffectiveHistoricConfigurationRecord.class;
    }

    /**
     * The column <code>public.effective_historic_configuration.id</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, UUID> ID = createField(DSL.name("id"), SQLDataType.UUID, this, "");

    /**
     * The column <code>public.effective_historic_configuration.task_id</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, UUID> TASK_ID = createField(DSL.name("task_id"), SQLDataType.UUID, this, "");

    /**
     * The column <code>public.effective_historic_configuration.log</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, String> LOG = createField(DSL.name("log"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.effective_historic_configuration.result</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, TaskExecutionResult> RESULT = createField(DSL.name("result"), SQLDataType.VARCHAR.asEnumDataType(place.sita.labelle.jooq.enums.TaskExecutionResult.class), this, "");

    /**
     * The column
     * <code>public.effective_historic_configuration.finished_at</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, OffsetDateTime> FINISHED_AT = createField(DSL.name("finished_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6), this, "");

    /**
     * The column
     * <code>public.effective_historic_configuration.started_at</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, OffsetDateTime> STARTED_AT = createField(DSL.name("started_at"), SQLDataType.TIMESTAMPWITHTIMEZONE(6), this, "");

    /**
     * The column
     * <code>public.effective_historic_configuration.configuration</code>.
     */
    public final TableField<EffectiveHistoricConfigurationRecord, String> CONFIGURATION = createField(DSL.name("configuration"), SQLDataType.CLOB, this, "");

    private EffectiveHistoricConfiguration(Name alias, Table<EffectiveHistoricConfigurationRecord> aliased) {
        this(alias, aliased, null);
    }

    private EffectiveHistoricConfiguration(Name alias, Table<EffectiveHistoricConfigurationRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.view("create view \"effective_historic_configuration\" as  SELECT task_execution.id,\n    task_execution.task_id,\n    task_execution.log,\n    task_execution.result,\n    task_execution.finished_at,\n    task_execution.started_at,\n    COALESCE(task_execution.configuration, task_config.config) AS configuration\n   FROM (task_execution\n     LEFT JOIN task_config ON ((task_config.task_id = task_execution.task_id)));"));
    }

    /**
     * Create an aliased <code>public.effective_historic_configuration</code>
     * table reference
     */
    public EffectiveHistoricConfiguration(String alias) {
        this(DSL.name(alias), EFFECTIVE_HISTORIC_CONFIGURATION);
    }

    /**
     * Create an aliased <code>public.effective_historic_configuration</code>
     * table reference
     */
    public EffectiveHistoricConfiguration(Name alias) {
        this(alias, EFFECTIVE_HISTORIC_CONFIGURATION);
    }

    /**
     * Create a <code>public.effective_historic_configuration</code> table
     * reference
     */
    public EffectiveHistoricConfiguration() {
        this(DSL.name("effective_historic_configuration"), null);
    }

    public <O extends Record> EffectiveHistoricConfiguration(Table<O> child, ForeignKey<O, EffectiveHistoricConfigurationRecord> key) {
        super(child, key, EFFECTIVE_HISTORIC_CONFIGURATION);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public EffectiveHistoricConfiguration as(String alias) {
        return new EffectiveHistoricConfiguration(DSL.name(alias), this);
    }

    @Override
    public EffectiveHistoricConfiguration as(Name alias) {
        return new EffectiveHistoricConfiguration(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public EffectiveHistoricConfiguration rename(String name) {
        return new EffectiveHistoricConfiguration(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public EffectiveHistoricConfiguration rename(Name name) {
        return new EffectiveHistoricConfiguration(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<UUID, UUID, String, TaskExecutionResult, OffsetDateTime, OffsetDateTime, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
