/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables;


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
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import place.sita.labelle.jooq.Public;
import place.sita.labelle.jooq.tables.records.TagDeltaCalcRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagDeltaCalc extends TableImpl<TagDeltaCalcRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.tag_delta_calc</code>
     */
    public static final TagDeltaCalc TAG_DELTA_CALC = new TagDeltaCalc();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TagDeltaCalcRecord> getRecordType() {
        return TagDeltaCalcRecord.class;
    }

    /**
     * The column <code>public.tag_delta_calc.added</code>.
     */
    public final TableField<TagDeltaCalcRecord, Boolean> ADDED = createField(DSL.name("added"), SQLDataType.BOOLEAN, this, "");

    /**
     * The column <code>public.tag_delta_calc.source</code>.
     */
    public final TableField<TagDeltaCalcRecord, String> SOURCE = createField(DSL.name("source"), SQLDataType.VARCHAR(256), this, "");

    /**
     * The column <code>public.tag_delta_calc.tag</code>.
     */
    public final TableField<TagDeltaCalcRecord, String> TAG = createField(DSL.name("tag"), SQLDataType.VARCHAR(256), this, "");

    /**
     * The column <code>public.tag_delta_calc.image_id</code>.
     */
    public final TableField<TagDeltaCalcRecord, UUID> IMAGE_ID = createField(DSL.name("image_id"), SQLDataType.UUID, this, "");

    private TagDeltaCalc(Name alias, Table<TagDeltaCalcRecord> aliased) {
        this(alias, aliased, null);
    }

    private TagDeltaCalc(Name alias, Table<TagDeltaCalcRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.view("create view \"tag_delta_calc\" as  SELECT delta.added,\n    delta.source,\n    delta.tag,\n    delta.image_id\n   FROM ( SELECT true AS added,\n            child.tag_family AS source,\n            child.tag_value AS tag,\n            child.image_id\n           FROM (image_tags child\n             JOIN image child_im ON ((child.image_id = child_im.id)))\n          WHERE ((child_im.parent_reference IS NOT NULL) AND (NOT (EXISTS ( SELECT 1\n                   FROM (image_tags parent\n                     JOIN parent_child_image pci ON ((pci.parent_image_id = parent.image_id)))\n                  WHERE ((pci.child_image_id = child.image_id) AND ((child.tag_value)::text = (parent.tag_value)::text) AND ((child.tag_family)::text = (parent.tag_family)::text))))))\n        UNION\n         SELECT false AS added,\n            parent.tag_family AS source,\n            parent.tag_value AS tag,\n            pci.child_image_id AS image_id\n           FROM (image_tags parent\n             JOIN parent_child_image pci ON ((pci.parent_image_id = parent.image_id)))\n          WHERE (NOT (EXISTS ( SELECT 1\n                   FROM image_tags child\n                  WHERE ((child.image_id = pci.child_image_id) AND ((child.tag_value)::text = (parent.tag_value)::text) AND ((child.tag_family)::text = (parent.tag_family)::text)))))) delta;"));
    }

    /**
     * Create an aliased <code>public.tag_delta_calc</code> table reference
     */
    public TagDeltaCalc(String alias) {
        this(DSL.name(alias), TAG_DELTA_CALC);
    }

    /**
     * Create an aliased <code>public.tag_delta_calc</code> table reference
     */
    public TagDeltaCalc(Name alias) {
        this(alias, TAG_DELTA_CALC);
    }

    /**
     * Create a <code>public.tag_delta_calc</code> table reference
     */
    public TagDeltaCalc() {
        this(DSL.name("tag_delta_calc"), null);
    }

    public <O extends Record> TagDeltaCalc(Table<O> child, ForeignKey<O, TagDeltaCalcRecord> key) {
        super(child, key, TAG_DELTA_CALC);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public TagDeltaCalc as(String alias) {
        return new TagDeltaCalc(DSL.name(alias), this);
    }

    @Override
    public TagDeltaCalc as(Name alias) {
        return new TagDeltaCalc(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public TagDeltaCalc rename(String name) {
        return new TagDeltaCalc(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public TagDeltaCalc rename(Name name) {
        return new TagDeltaCalc(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Boolean, String, String, UUID> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
