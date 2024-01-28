package place.sita.labelle.core.preferences;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static place.sita.labelle.jooq.Tables.PREFERENCES;

@Component
public class Preferences {

    private final DSLContext context;

    public Preferences(DSLContext context) {
        this.context = context;
    }


    public <T> T getPreference(PreferenceType<T> preferenceType) {
        String preferences = context
                .select(PREFERENCES.VALUE)
                .from(PREFERENCES)
                .where(PREFERENCES.NAME.eq(preferenceType.key()))
                .fetchOne(PREFERENCES.VALUE);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.convertValue(preferences, preferenceType.type());
    }


}
