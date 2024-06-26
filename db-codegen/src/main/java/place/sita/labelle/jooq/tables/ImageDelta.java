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
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import place.sita.labelle.jooq.Keys;
import place.sita.labelle.jooq.Public;
import place.sita.labelle.jooq.tables.records.ImageDeltaRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ImageDelta extends TableImpl<ImageDeltaRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.image_delta</code>
     */
    public static final ImageDelta IMAGE_DELTA = new ImageDelta();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ImageDeltaRecord> getRecordType() {
        return ImageDeltaRecord.class;
    }

    /**
     * The column <code>public.image_delta.image_id</code>.
     */
    public final TableField<ImageDeltaRecord, UUID> IMAGE_ID = createField(DSL.name("image_id"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.image_delta.image_resolvable_id</code>.
     */
    public final TableField<ImageDeltaRecord, UUID> IMAGE_RESOLVABLE_ID = createField(DSL.name("image_resolvable_id"), SQLDataType.UUID.nullable(false), this, "");

    private ImageDelta(Name alias, Table<ImageDeltaRecord> aliased) {
        this(alias, aliased, null);
    }

    private ImageDelta(Name alias, Table<ImageDeltaRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.image_delta</code> table reference
     */
    public ImageDelta(String alias) {
        this(DSL.name(alias), IMAGE_DELTA);
    }

    /**
     * Create an aliased <code>public.image_delta</code> table reference
     */
    public ImageDelta(Name alias) {
        this(alias, IMAGE_DELTA);
    }

    /**
     * Create a <code>public.image_delta</code> table reference
     */
    public ImageDelta() {
        this(DSL.name("image_delta"), null);
    }

    public <O extends Record> ImageDelta(Table<O> child, ForeignKey<O, ImageDeltaRecord> key) {
        super(child, key, IMAGE_DELTA);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public List<ForeignKey<ImageDeltaRecord, ?>> getReferences() {
        return Arrays.asList(Keys.IMAGE_DELTA__FK_IMAGE_DELTA_IMAGE_ID, Keys.IMAGE_DELTA__FK_IMAGE_DELTA_IMAGE_RESOLVABLE_ID);
    }

    private transient Image _image;
    private transient ImageResolvable _imageResolvable;

    public Image image() {
        if (_image == null)
            _image = new Image(this, Keys.IMAGE_DELTA__FK_IMAGE_DELTA_IMAGE_ID);

        return _image;
    }

    public ImageResolvable imageResolvable() {
        if (_imageResolvable == null)
            _imageResolvable = new ImageResolvable(this, Keys.IMAGE_DELTA__FK_IMAGE_DELTA_IMAGE_RESOLVABLE_ID);

        return _imageResolvable;
    }

    @Override
    public ImageDelta as(String alias) {
        return new ImageDelta(DSL.name(alias), this);
    }

    @Override
    public ImageDelta as(Name alias) {
        return new ImageDelta(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ImageDelta rename(String name) {
        return new ImageDelta(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ImageDelta rename(Name name) {
        return new ImageDelta(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<UUID, UUID> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
