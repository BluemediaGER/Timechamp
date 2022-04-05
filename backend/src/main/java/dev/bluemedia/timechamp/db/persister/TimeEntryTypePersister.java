package dev.bluemedia.timechamp.db.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import dev.bluemedia.timechamp.model.type.TimeEntryType;

/**
 * Custom ORMLite persister used to convert {@link TimeEntryType} to a database friendly format.
 *
 * @author Oliver Traber
 */
public class TimeEntryTypePersister extends StringType {

    private static final TimeEntryTypePersister singleton = new TimeEntryTypePersister();

    private TimeEntryTypePersister() {
            super(SqlType.STRING, new Class<?>[] { TimeEntryType.class });
    }

    public static TimeEntryTypePersister getSingleton() {
            return singleton;
        }

    /**
     * Convert an {@link TimeEntryType} to it's string representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return String representation of the given {@link TimeEntryType}, or null if the given object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject instanceof TimeEntryType) {
            return ((TimeEntryType) javaObject).toTextValue();
        }
        throw new IllegalArgumentException("Given object is not of type TimeEntryType.");
    }

    /**
     * Convert an {@link String} back to it's {@link TimeEntryType} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return TimeEntryType.fromTextValue((String) sqlArg);
    }

}
