/*
 * This file is generated by jOOQ.
 */
package place.sita.labelle.jooq.tables.records;


import java.util.UUID;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;

import place.sita.labelle.jooq.tables.TagTranslation;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagTranslationRecord extends UpdatableRecordImpl<TagTranslationRecord> implements Record4<UUID, String, String, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.tag_translation.repository_id</code>.
     */
    public void setRepositoryId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.tag_translation.repository_id</code>.
     */
    public UUID getRepositoryId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>public.tag_translation.tag_level_translation</code>.
     */
    public void setTagLevelTranslation(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.tag_translation.tag_level_translation</code>.
     */
    public String getTagLevelTranslation() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.tag_translation.tags_level_translation</code>.
     */
    public void setTagsLevelTranslation(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.tag_translation.tags_level_translation</code>.
     */
    public String getTagsLevelTranslation() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.tag_translation.validation</code>.
     */
    public void setValidation(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.tag_translation.validation</code>.
     */
    public Integer getValidation() {
        return (Integer) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UUID, String, String, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UUID, String, String, Integer> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UUID> field1() {
        return TagTranslation.TAG_TRANSLATION.REPOSITORY_ID;
    }

    @Override
    public Field<String> field2() {
        return TagTranslation.TAG_TRANSLATION.TAG_LEVEL_TRANSLATION;
    }

    @Override
    public Field<String> field3() {
        return TagTranslation.TAG_TRANSLATION.TAGS_LEVEL_TRANSLATION;
    }

    @Override
    public Field<Integer> field4() {
        return TagTranslation.TAG_TRANSLATION.VALIDATION;
    }

    @Override
    public UUID component1() {
        return getRepositoryId();
    }

    @Override
    public String component2() {
        return getTagLevelTranslation();
    }

    @Override
    public String component3() {
        return getTagsLevelTranslation();
    }

    @Override
    public Integer component4() {
        return getValidation();
    }

    @Override
    public UUID value1() {
        return getRepositoryId();
    }

    @Override
    public String value2() {
        return getTagLevelTranslation();
    }

    @Override
    public String value3() {
        return getTagsLevelTranslation();
    }

    @Override
    public Integer value4() {
        return getValidation();
    }

    @Override
    public TagTranslationRecord value1(UUID value) {
        setRepositoryId(value);
        return this;
    }

    @Override
    public TagTranslationRecord value2(String value) {
        setTagLevelTranslation(value);
        return this;
    }

    @Override
    public TagTranslationRecord value3(String value) {
        setTagsLevelTranslation(value);
        return this;
    }

    @Override
    public TagTranslationRecord value4(Integer value) {
        setValidation(value);
        return this;
    }

    @Override
    public TagTranslationRecord values(UUID value1, String value2, String value3, Integer value4) {
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
     * Create a detached TagTranslationRecord
     */
    public TagTranslationRecord() {
        super(TagTranslation.TAG_TRANSLATION);
    }

    /**
     * Create a detached, initialised TagTranslationRecord
     */
    public TagTranslationRecord(UUID repositoryId, String tagLevelTranslation, String tagsLevelTranslation, Integer validation) {
        super(TagTranslation.TAG_TRANSLATION);

        setRepositoryId(repositoryId);
        setTagLevelTranslation(tagLevelTranslation);
        setTagsLevelTranslation(tagsLevelTranslation);
        setValidation(validation);
    }
}
