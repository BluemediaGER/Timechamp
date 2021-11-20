package dev.bluemedia.timechamp.db.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import dev.bluemedia.timechamp.model.type.Permission;

/**
 * Custom ORMLite persister used to convert {@link Permission} to a database friendly format.
 *
 * @author Oliver Traber
 */
public class PermissionPersister extends StringType {

    private static final PermissionPersister singleton = new PermissionPersister();

    private PermissionPersister() {
        super(SqlType.STRING, new Class<?>[] { Permission.class });
    }

    public static PermissionPersister getSingleton() {
        return singleton;
    }

    /**
     * Convert an {@link Permission} to it's string representation for persistence.
     * @param fieldType Type of the SQL field where the object should be persisted.
     * @param javaObject Object that should be persisted.
     * @return String representation of the given {@link Permission}, or null if the given object was null.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject instanceof Permission) {
            return ((Permission) javaObject).toTextValue();
        }
        throw new IllegalArgumentException("Given object is not of type Permission.");
    }

    /**
     * Convert an {@link String} back to it's {@link Permission} representation.
     * @param fieldType Type of the SQL field where the object was persisted.
     * @param sqlArg Object that was persisted.
     * @return Java object representing the given sql type.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return Permission.fromTextValue((String) sqlArg);
    }

}
