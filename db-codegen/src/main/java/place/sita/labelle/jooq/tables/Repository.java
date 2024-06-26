/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
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
import place.sita.labelle.jooq.tables.records.RepositoryRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Repository extends TableImpl<RepositoryRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.repository</code>
     */
    public static final Repository REPOSITORY = new Repository();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RepositoryRecord> getRecordType() {
        return RepositoryRecord.class;
    }

    /**
     * The column <code>public.repository.id</code>.
     */
    public final TableField<RepositoryRecord, UUID> ID = createField(DSL.name("id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.repository.name</code>.
     */
    public final TableField<RepositoryRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "");

    private Repository(Name alias, Table<RepositoryRecord> aliased) {
        this(alias, aliased, null);
    }

    private Repository(Name alias, Table<RepositoryRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.repository</code> table reference
     */
    public Repository(String alias) {
        this(DSL.name(alias), REPOSITORY);
    }

    /**
     * Create an aliased <code>public.repository</code> table reference
     */
    public Repository(Name alias) {
        this(alias, REPOSITORY);
    }

    /**
     * Create a <code>public.repository</code> table reference
     */
    public Repository() {
        this(DSL.name("repository"), null);
    }

    public <O extends Record> Repository(Table<O> child, ForeignKey<O, RepositoryRecord> key) {
        super(child, key, REPOSITORY);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<RepositoryRecord> getPrimaryKey() {
        return Keys.CATALOGUE_PKEY;
    }

    @Override
    public Repository as(String alias) {
        return new Repository(DSL.name(alias), this);
    }

    @Override
    public Repository as(Name alias) {
        return new Repository(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Repository rename(String name) {
        return new Repository(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Repository rename(Name name) {
        return new Repository(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<UUID, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
