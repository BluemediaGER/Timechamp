package dev.bluemedia.timechamp.db.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.IntType;
import dev.bluemedia.timechamp.model.type.Permission;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Custom ORMLite persister used to convert {@link Permission} to a database friendly format.
 *
 * @author Oliver Traber
 */
public class DurationPersister extends IntType {

    private static final DurationPersister singleton = new DurationPersister();

    private DurationPersister() {
        super(SqlType.INTEGER, new Class<?>[] { Duration.class });
    }

    public static DurationPersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link Duration} to it's integer representation (seconds) for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return Integer representation in seconds of the given {@link Duration}, or null if the given object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        Duration duration = (Duration) javaObject;
        if (duration == null) {
            return null;
        } else {
            return duration.get(ChronoUnit.SECONDS);
        }
    }

    /**
     * Convert an {@link Integer} back to it's {@link Duration} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return Duration.ofSeconds((Integer) sqlArg);
    }
}
