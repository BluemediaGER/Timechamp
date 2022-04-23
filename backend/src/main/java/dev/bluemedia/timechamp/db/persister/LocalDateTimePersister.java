package dev.bluemedia.timechamp.db.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.TimeStampType;

import java.sql.Timestamp;
import java.time.LocalDateTime;


/**
 * Custom ORMLite persister used to convert {@link LocalDateTime} to a database friendly format.
 *
 * @author Oliver Traber
 */
public class LocalDateTimePersister extends TimeStampType {

    private static final LocalDateTimePersister singleton = new LocalDateTimePersister();

    private LocalDateTimePersister() {
        super(SqlType.DATE, new Class<?>[] { LocalDateTime.class });
    }

    public static LocalDateTimePersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link LocalDateTime} to it's {@link Timestamp} representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return {@link Timestamp} representation of the given {@link LocalDateTime}, or null if the object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        }
        if (javaObject instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) javaObject);
        }
        throw new IllegalArgumentException("Expected LocalDateTime, got " + javaObject.getClass());
    }

    /**
     * Convert an {@link Timestamp} back to it's {@link LocalDateTime} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        Timestamp timestamp = (Timestamp) sqlArg;
        return timestamp.toLocalDateTime();
    }
}

