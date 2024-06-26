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
import org.jooq.Row8;
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
import place.sita.labelle.jooq.tables.records.ImageRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Image extends TableImpl<ImageRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.image</code>
     */
    public static final Image IMAGE = new Image();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ImageRecord> getRecordType() {
        return ImageRecord.class;
    }

    /**
     * The column <code>public.image.image_resolvable_id</code>.
     */
    public final TableField<ImageRecord, UUID> IMAGE_RESOLVABLE_ID = createField(DSL.name("image_resolvable_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.image.repository_id</code>.
     */
    public final TableField<ImageRecord, UUID> REPOSITORY_ID = createField(DSL.name("repository_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.image.id</code>.
     */
    public final TableField<ImageRecord, UUID> ID = createField(DSL.name("id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.image.reference_id</code>.
     */
    public final TableField<ImageRecord, String> REFERENCE_ID = createField(DSL.name("reference_id"), SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.image.parent_reference</code>.
     */
    public final TableField<ImageRecord, String> PARENT_REFERENCE = createField(DSL.name("parent_reference"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.image.use_tag_delta</code>.
     */
    public final TableField<ImageRecord, Boolean> USE_TAG_DELTA = createField(DSL.name("use_tag_delta"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("true", SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.image.use_image_delta</code>.
     */
    public final TableField<ImageRecord, Boolean> USE_IMAGE_DELTA = createField(DSL.name("use_image_delta"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("true", SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.image.visible_to_children</code>.
     */
    public final TableField<ImageRecord, Boolean> VISIBLE_TO_CHILDREN = createField(DSL.name("visible_to_children"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("true", SQLDataType.BOOLEAN)), this, "");

    private Image(Name alias, Table<ImageRecord> aliased) {
        this(alias, aliased, null);
    }

    private Image(Name alias, Table<ImageRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.image</code> table reference
     */
    public Image(String alias) {
        this(DSL.name(alias), IMAGE);
    }

    /**
     * Create an aliased <code>public.image</code> table reference
     */
    public Image(Name alias) {
        this(alias, IMAGE);
    }

    /**
     * Create a <code>public.image</code> table reference
     */
    public Image() {
        this(DSL.name("image"), null);
    }

    public <O extends Record> Image(Table<O> child, ForeignKey<O, ImageRecord> key) {
        super(child, key, IMAGE);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<ImageRecord> getPrimaryKey() {
        return Keys.IMAGE_PKEY;
    }

    @Override
    public List<UniqueKey<ImageRecord>> getUniqueKeys() {
        return Arrays.asList(Keys.UQ_IMAGE_REFERENCE_ID_IN_REPOSITORY);
    }

    @Override
    public List<ForeignKey<ImageRecord, ?>> getReferences() {
        return Arrays.asList(Keys.IMAGE__FK_IMAGE_IMAGE_RESOLVABLE_ID, Keys.IMAGE__FK_IMAGE_REPOSITORY_ID);
    }

    private transient ImageResolvable _imageResolvable;
    private transient Repository _repository;

    public ImageResolvable imageResolvable() {
        if (_imageResolvable == null)
            _imageResolvable = new ImageResolvable(this, Keys.IMAGE__FK_IMAGE_IMAGE_RESOLVABLE_ID);

        return _imageResolvable;
    }

    public Repository repository() {
        if (_repository == null)
            _repository = new Repository(this, Keys.IMAGE__FK_IMAGE_REPOSITORY_ID);

        return _repository;
    }

    @Override
    public Image as(String alias) {
        return new Image(DSL.name(alias), this);
    }

    @Override
    public Image as(Name alias) {
        return new Image(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Image rename(String name) {
        return new Image(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Image rename(Name name) {
        return new Image(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<UUID, UUID, UUID, String, String, Boolean, Boolean, Boolean> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
