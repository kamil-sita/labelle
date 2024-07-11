/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
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
import place.sita.labelle.jooq.tables.records.TagRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tag extends TableImpl<TagRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.tag</code>
     */
    public static final Tag TAG = new Tag();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TagRecord> getRecordType() {
        return TagRecord.class;
    }

    /**
     * The column <code>public.tag.value</code>.
     */
    public final TableField<TagRecord, String> VALUE = createField(DSL.name("value"), SQLDataType.VARCHAR(256), this, "");

    /**
     * The column <code>public.tag.id</code>.
     */
    public final TableField<TagRecord, UUID> ID = createField(DSL.name("id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.tag.tag_category_id</code>.
     */
    public final TableField<TagRecord, UUID> TAG_CATEGORY_ID = createField(DSL.name("tag_category_id"), SQLDataType.UUID.nullable(false), this, "");

    private Tag(Name alias, Table<TagRecord> aliased) {
        this(alias, aliased, null);
    }

    private Tag(Name alias, Table<TagRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.tag</code> table reference
     */
    public Tag(String alias) {
        this(DSL.name(alias), TAG);
    }

    /**
     * Create an aliased <code>public.tag</code> table reference
     */
    public Tag(Name alias) {
        this(alias, TAG);
    }

    /**
     * Create a <code>public.tag</code> table reference
     */
    public Tag() {
        this(DSL.name("tag"), null);
    }

    public <O extends Record> Tag(Table<O> child, ForeignKey<O, TagRecord> key) {
        super(child, key, TAG);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<TagRecord> getPrimaryKey() {
        return Keys.TAG_PKEY;
    }

    @Override
    public List<ForeignKey<TagRecord, ?>> getReferences() {
        return Arrays.asList(Keys.TAG__FK_TAG_TAG_SRC_ID_3b);
    }

    private transient TagCategory _tagCategory;

    public TagCategory tagCategory() {
        if (_tagCategory == null)
            _tagCategory = new TagCategory(this, Keys.TAG__FK_TAG_TAG_SRC_ID_3b);

        return _tagCategory;
    }

    @Override
    public Tag as(String alias) {
        return new Tag(DSL.name(alias), this);
    }

    @Override
    public Tag as(Name alias) {
        return new Tag(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Tag rename(String name) {
        return new Tag(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Tag rename(Name name) {
        return new Tag(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, UUID, UUID> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
