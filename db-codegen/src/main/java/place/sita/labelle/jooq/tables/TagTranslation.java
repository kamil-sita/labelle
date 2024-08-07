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
import org.jooq.Row4;
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
import place.sita.labelle.jooq.tables.records.TagTranslationRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagTranslation extends TableImpl<TagTranslationRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.tag_translation</code>
     */
    public static final TagTranslation TAG_TRANSLATION = new TagTranslation();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TagTranslationRecord> getRecordType() {
        return TagTranslationRecord.class;
    }

    /**
     * The column <code>public.tag_translation.repository_id</code>.
     */
    public final TableField<TagTranslationRecord, UUID> REPOSITORY_ID = createField(DSL.name("repository_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.tag_translation.tag_level_translation</code>.
     */
    public final TableField<TagTranslationRecord, String> TAG_LEVEL_TRANSLATION = createField(DSL.name("tag_level_translation"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.tag_translation.tags_level_translation</code>.
     */
    public final TableField<TagTranslationRecord, String> TAGS_LEVEL_TRANSLATION = createField(DSL.name("tags_level_translation"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.tag_translation.validation</code>.
     */
    public final TableField<TagTranslationRecord, Integer> VALIDATION = createField(DSL.name("validation"), SQLDataType.INTEGER, this, "");

    private TagTranslation(Name alias, Table<TagTranslationRecord> aliased) {
        this(alias, aliased, null);
    }

    private TagTranslation(Name alias, Table<TagTranslationRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.tag_translation</code> table reference
     */
    public TagTranslation(String alias) {
        this(DSL.name(alias), TAG_TRANSLATION);
    }

    /**
     * Create an aliased <code>public.tag_translation</code> table reference
     */
    public TagTranslation(Name alias) {
        this(alias, TAG_TRANSLATION);
    }

    /**
     * Create a <code>public.tag_translation</code> table reference
     */
    public TagTranslation() {
        this(DSL.name("tag_translation"), null);
    }

    public <O extends Record> TagTranslation(Table<O> child, ForeignKey<O, TagTranslationRecord> key) {
        super(child, key, TAG_TRANSLATION);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<TagTranslationRecord> getPrimaryKey() {
        return Keys.TAG_TRANSLATION_PKEY;
    }

    @Override
    public List<ForeignKey<TagTranslationRecord, ?>> getReferences() {
        return Arrays.asList(Keys.TAG_TRANSLATION__FK_TAG_TRANSLATION_REPOSITORY_ID);
    }

    private transient Repository _repository;

    public Repository repository() {
        if (_repository == null)
            _repository = new Repository(this, Keys.TAG_TRANSLATION__FK_TAG_TRANSLATION_REPOSITORY_ID);

        return _repository;
    }

    @Override
    public TagTranslation as(String alias) {
        return new TagTranslation(DSL.name(alias), this);
    }

    @Override
    public TagTranslation as(Name alias) {
        return new TagTranslation(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TagTranslation rename(String name) {
        return new TagTranslation(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TagTranslation rename(Name name) {
        return new TagTranslation(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<UUID, String, String, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
