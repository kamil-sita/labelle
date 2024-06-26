/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.TableRecordImpl;

import place.sita.labelle.jooq.tables.ImageTags;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ImageTagsRecord extends TableRecordImpl<ImageTagsRecord> implements Record4<String, String, UUID, UUID> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.image_tags.tag_value</code>.
     */
    public void setTagValue(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.image_tags.tag_value</code>.
     */
    public String getTagValue() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.image_tags.tag_family</code>.
     */
    public void setTagFamily(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.image_tags.tag_family</code>.
     */
    public String getTagFamily() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.image_tags.image_id</code>.
     */
    public void setImageId(UUID value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.image_tags.image_id</code>.
     */
    public UUID getImageId() {
        return (UUID) get(2);
    }

    /**
     * Setter for <code>public.image_tags.repository_id</code>.
     */
    public void setRepositoryId(UUID value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.image_tags.repository_id</code>.
     */
    public UUID getRepositoryId() {
        return (UUID) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<String, String, UUID, UUID> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<String, String, UUID, UUID> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return ImageTags.IMAGE_TAGS.TAG_VALUE;
    }

    @Override
    public Field<String> field2() {
        return ImageTags.IMAGE_TAGS.TAG_FAMILY;
    }

    @Override
    public Field<UUID> field3() {
        return ImageTags.IMAGE_TAGS.IMAGE_ID;
    }

    @Override
    public Field<UUID> field4() {
        return ImageTags.IMAGE_TAGS.REPOSITORY_ID;
    }

    @Override
    public String component1() {
        return getTagValue();
    }

    @Override
    public String component2() {
        return getTagFamily();
    }

    @Override
    public UUID component3() {
        return getImageId();
    }

    @Override
    public UUID component4() {
        return getRepositoryId();
    }

    @Override
    public String value1() {
        return getTagValue();
    }

    @Override
    public String value2() {
        return getTagFamily();
    }

    @Override
    public UUID value3() {
        return getImageId();
    }

    @Override
    public UUID value4() {
        return getRepositoryId();
    }

    @Override
    public ImageTagsRecord value1(String value) {
        setTagValue(value);
        return this;
    }

    @Override
    public ImageTagsRecord value2(String value) {
        setTagFamily(value);
        return this;
    }

    @Override
    public ImageTagsRecord value3(UUID value) {
        setImageId(value);
        return this;
    }

    @Override
    public ImageTagsRecord value4(UUID value) {
        setRepositoryId(value);
        return this;
    }

    @Override
    public ImageTagsRecord values(String value1, String value2, UUID value3, UUID value4) {
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
     * Create a detached ImageTagsRecord
     */
    public ImageTagsRecord() {
        super(ImageTags.IMAGE_TAGS);
    }

    /**
     * Create a detached, initialised ImageTagsRecord
     */
    public ImageTagsRecord(String tagValue, String tagFamily, UUID imageId, UUID repositoryId) {
        super(ImageTags.IMAGE_TAGS);

        setTagValue(tagValue);
        setTagFamily(tagFamily);
        setImageId(imageId);
        setRepositoryId(repositoryId);
    }
}
