/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.TableRecordImpl;

import place.sita.labelle.jooq.tables.Marker;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MarkerRecord extends TableRecordImpl<MarkerRecord> implements Record4<UUID, String, String, Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.marker.image_id</code>.
     */
    public void setImageId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.marker.image_id</code>.
     */
    public UUID getImageId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.marker.family</code>.
     */
    public void setFamily(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.marker.family</code>.
     */
    public String getFamily() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.marker.value</code>.
     */
    public void setValue(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.marker.value</code>.
     */
    public String getValue() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.marker.shared</code>.
     */
    public void setShared(Boolean value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.marker.shared</code>.
     */
    public Boolean getShared() {
        return (Boolean) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UUID, String, String, Boolean> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UUID, String, String, Boolean> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return Marker.MARKER.IMAGE_ID;
    }

    @Override
    public Field<String> field2() {
        return Marker.MARKER.FAMILY;
    }

    @Override
    public Field<String> field3() {
        return Marker.MARKER.VALUE;
    }

    @Override
    public Field<Boolean> field4() {
        return Marker.MARKER.SHARED;
    }

    @Override
    public UUID component1() {
        return getImageId();
    }

    @Override
    public String component2() {
        return getFamily();
    }

    @Override
    public String component3() {
        return getValue();
    }

    @Override
    public Boolean component4() {
        return getShared();
    }

    @Override
    public UUID value1() {
        return getImageId();
    }

    @Override
    public String value2() {
        return getFamily();
    }

    @Override
    public String value3() {
        return getValue();
    }

    @Override
    public Boolean value4() {
        return getShared();
    }

    @Override
    public MarkerRecord value1(UUID value) {
        setImageId(value);
        return this;
    }

    @Override
    public MarkerRecord value2(String value) {
        setFamily(value);
        return this;
    }

    @Override
    public MarkerRecord value3(String value) {
        setValue(value);
        return this;
    }

    @Override
    public MarkerRecord value4(Boolean value) {
        setShared(value);
        return this;
    }

    @Override
    public MarkerRecord values(UUID value1, String value2, String value3, Boolean value4) {
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
     * Create a detached MarkerRecord
     */
    public MarkerRecord() {
        super(Marker.MARKER);
    }

    /**
     * Create a detached, initialised MarkerRecord
     */
    public MarkerRecord(UUID imageId, String family, String value, Boolean shared) {
        super(Marker.MARKER);

        setImageId(imageId);
        setFamily(family);
        setValue(value);
        setShared(shared);
    }
}
