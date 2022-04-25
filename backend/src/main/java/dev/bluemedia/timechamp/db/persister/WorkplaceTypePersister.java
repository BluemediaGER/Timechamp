package dev.bluemedia.timechamp.db.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import dev.bluemedia.timechamp.model.type.WorkplaceType;

/**
 * Custom ORMLite persister used to convert {@link WorkplaceType} to a database friendly format.
 *
 * @author Oliver Traber
 */
public class WorkplaceTypePersister extends StringType {

    private static final WorkplaceTypePersister singleton = new WorkplaceTypePersister();

    private WorkplaceTypePersister() {
        super(SqlType.STRING, new Class<?>[] { WorkplaceType.class });
    }

    public static WorkplaceTypePersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link WorkplaceType} to it's string representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return String representation of the given {@link WorkplaceType}, or null if the given object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject instanceof WorkplaceType) {
            return ((WorkplaceType) javaObject).toTextValue();
        }
        throw new IllegalArgumentException("Given object is not of type WorkplaceType.");
    }

    /**
     * Convert an {@link String} back to it's {@link WorkplaceType} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return WorkplaceType.fromTextValue((String) sqlArg);
    }

}
