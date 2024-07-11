/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.TableRecordImpl;

import place.sita.labelle.jooq.tables.TagDelta;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagDeltaRecord extends TableRecordImpl<TagDeltaRecord> implements Record4<UUID, Boolean, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.tag_delta.image_id</code>.
     */
    public void setImageId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.tag_delta.image_id</code>.
     */
    public UUID getImageId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.tag_delta.adds</code>.
     */
    public void setAdds(Boolean value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.tag_delta.adds</code>.
     */
    public Boolean getAdds() {
        return (Boolean) get(1);
    }

    /**
     * Setter for <code>public.tag_delta.category</code>.
     */
    public void setCategory(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.tag_delta.category</code>.
     */
    public String getCategory() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.tag_delta.tag</code>.
     */
    public void setTag(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.tag_delta.tag</code>.
     */
    public String getTag() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UUID, Boolean, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UUID, Boolean, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return TagDelta.TAG_DELTA.IMAGE_ID;
    }

    @Override
    public Field<Boolean> field2() {
        return TagDelta.TAG_DELTA.ADDS;
    }

    @Override
    public Field<String> field3() {
        return TagDelta.TAG_DELTA.CATEGORY;
    }

    @Override
    public Field<String> field4() {
        return TagDelta.TAG_DELTA.TAG;
    }

    @Override
    public UUID component1() {
        return getImageId();
    }

    @Override
    public Boolean component2() {
        return getAdds();
    }

    @Override
    public String component3() {
        return getCategory();
    }

    @Override
    public String component4() {
        return getTag();
    }

    @Override
    public UUID value1() {
        return getImageId();
    }

    @Override
    public Boolean value2() {
        return getAdds();
    }

    @Override
    public String value3() {
        return getCategory();
    }

    @Override
    public String value4() {
        return getTag();
    }

    @Override
    public TagDeltaRecord value1(UUID value) {
        setImageId(value);
        return this;
    }

    @Override
    public TagDeltaRecord value2(Boolean value) {
        setAdds(value);
        return this;
    }

    @Override
    public TagDeltaRecord value3(String value) {
        setCategory(value);
        return this;
    }

    @Override
    public TagDeltaRecord value4(String value) {
        setTag(value);
        return this;
    }

    @Override
    public TagDeltaRecord values(UUID value1, Boolean value2, String value3, String value4) {
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
     * Create a detached TagDeltaRecord
     */
    public TagDeltaRecord() {
        super(TagDelta.TAG_DELTA);
    }

    /**
     * Create a detached, initialised TagDeltaRecord
     */
    public TagDeltaRecord(UUID imageId, Boolean adds, String category, String tag) {
        super(TagDelta.TAG_DELTA);

        setImageId(imageId);
        setAdds(adds);
        setCategory(category);
        setTag(tag);
    }
}
