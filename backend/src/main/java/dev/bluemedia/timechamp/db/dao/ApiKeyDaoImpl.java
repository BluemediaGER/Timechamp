package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.ApiKey;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * DAO to access and manipulate {@link ApiKey} objects.
 *
 * @author Oliver Traber
 */
public class ApiKeyDaoImpl extends GenericDao<ApiKey> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public ApiKeyDaoImpl(Dao<ApiKey, UUID> dao) {
        super(dao);
    }

    /**
     * Get all {@link ApiKey} objects that belong to the specified user.
     * @param parentUserId Id of the user whose keys should be retrieved.
     * @return List of {@link ApiKey} objects which belong to the specified user.
     */
    public List<ApiKey> getByParentUser(UUID parentUserId) {
        return getAllByAttributeMatch("parentUserId", parentUserId);
    }

    /**
     * Remove all {@link ApiKey} objects that belong to the specified user.
     * @param parentUserId Id of the user whose keys should be removed.
     * @throws SQLException Exception thrown when an error occurs during deletion.
     */
    public void removeAllKeysOfUser(UUID parentUserId) throws SQLException {
        List<ApiKey> keysToDelete = getAllByAttributeMatch("parentUserId", parentUserId);
        dao.delete(keysToDelete);
    }

}
