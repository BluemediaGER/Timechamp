package dev.bluemedia.timechamp.db.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.IntegerObjectType;

import java.sql.Timestamp;
import java.time.LocalTime;

public class LocalTimePersister extends IntegerObjectType {

    private static final LocalTimePersister singleton = new LocalTimePersister();

    private LocalTimePersister() {
        super(SqlType.INTEGER, new Class<?>[] { LocalTime.class });
    }

    public static LocalTimePersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link LocalTime} to it's {@link Timestamp} representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return {@link Timestamp} representation of the given {@link LocalTime}, or null if the object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        }
        if (javaObject instanceof LocalTime) {
            return ((LocalTime) javaObject).toSecondOfDay();
        }
        throw new IllegalArgumentException("Expected LocalTime, got " + javaObject.getClass());
    }

    /**
     * Convert an {@link Timestamp} back to it's {@link LocalTime} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        int rawSqlArg = (int) sqlArg;
        return LocalTime.ofSecondOfDay(rawSqlArg);
    }
}
