/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.TableRecordImpl;

import place.sita.labelle.jooq.tables.TagDeltaCalc;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagDeltaCalcRecord extends TableRecordImpl<TagDeltaCalcRecord> implements Record4<Boolean, String, String, UUID> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.tag_delta_calc.added</code>.
     */
    public void setAdded(Boolean value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.tag_delta_calc.added</code>.
     */
    public Boolean getAdded() {
        return (Boolean) get(0);
    }

    /**
     * Setter for <code>public.tag_delta_calc.source</code>.
     */
    public void setSource(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.tag_delta_calc.source</code>.
     */
    public String getSource() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.tag_delta_calc.tag</code>.
     */
    public void setTag(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.tag_delta_calc.tag</code>.
     */
    public String getTag() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.tag_delta_calc.image_id</code>.
     */
    public void setImageId(UUID value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.tag_delta_calc.image_id</code>.
     */
    public UUID getImageId() {
        return (UUID) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Boolean, String, String, UUID> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Boolean, String, String, UUID> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Boolean> field1() {
        return TagDeltaCalc.TAG_DELTA_CALC.ADDED;
    }

    @Override
    public Field<String> field2() {
        return TagDeltaCalc.TAG_DELTA_CALC.SOURCE;
    }

    @Override
    public Field<String> field3() {
        return TagDeltaCalc.TAG_DELTA_CALC.TAG;
    }

    @Override
    public Field<UUID> field4() {
        return TagDeltaCalc.TAG_DELTA_CALC.IMAGE_ID;
    }

    @Override
    public Boolean component1() {
        return getAdded();
    }

    @Override
    public String component2() {
        return getSource();
    }

    @Override
    public String component3() {
        return getTag();
    }

    @Override
    public UUID component4() {
        return getImageId();
    }

    @Override
    public Boolean value1() {
        return getAdded();
    }

    @Override
    public String value2() {
        return getSource();
    }

    @Override
    public String value3() {
        return getTag();
    }

    @Override
    public UUID value4() {
        return getImageId();
    }

    @Override
    public TagDeltaCalcRecord value1(Boolean value) {
        setAdded(value);
        return this;
    }

    @Override
    public TagDeltaCalcRecord value2(String value) {
        setSource(value);
        return this;
    }

    @Override
    public TagDeltaCalcRecord value3(String value) {
        setTag(value);
        return this;
    }

    @Override
    public TagDeltaCalcRecord value4(UUID value) {
        setImageId(value);
        return this;
    }

    @Override
    public TagDeltaCalcRecord values(Boolean value1, String value2, String value3, UUID value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TagDeltaCalcRecord
     */
    public TagDeltaCalcRecord() {
        super(TagDeltaCalc.TAG_DELTA_CALC);
    }

    /**
     * Create a detached, initialised TagDeltaCalcRecord
     */
    public TagDeltaCalcRecord(Boolean added, String source, String tag, UUID imageId) {
        super(TagDeltaCalc.TAG_DELTA_CALC);

        setAdded(added);
        setSource(source);
        setTag(tag);
        setImageId(imageId);
    }
}