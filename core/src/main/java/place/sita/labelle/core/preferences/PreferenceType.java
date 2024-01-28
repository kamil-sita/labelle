package place.sita.labelle.core.preferences;

public interface PreferenceType<T> {

    String key();

    Class<T> type();

    T defaultValue();

}
