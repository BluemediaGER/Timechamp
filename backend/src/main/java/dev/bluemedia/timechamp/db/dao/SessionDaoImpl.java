package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.Session;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO to access and manipulate {@link Session} objects.
 *
 * @author Oliver Traber
 */
public class SessionDaoImpl extends GenericDao<Session> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public SessionDaoImpl(Dao<Session, String> dao) {
        super(dao);
    }

    /**
     * Get all {@link Session} objects that belong to the specified user.
     * @param parentUserId Id of the user whose keys should be retrieved.
     * @return List of {@link Session} objects which belong to the specified user.
     */
    public List<Session> getByParentUser(String parentUserId) {
        return getAllByAttributeMatch("parentUserId", parentUserId);
    }

    /**
     * Remove all {@link Session} objects that belong to the specified user.
     * @param parentUserId Id of the user whose keys should be removed.
     * @throws SQLException Exception thrown when an error occurs during deletion.
     */
    public void removeAllSessionsOfUser(String parentUserId) throws SQLException {
        List<Session> sessionsToDelete = getAllByAttributeMatch("parentUserId", parentUserId);
        dao.delete(sessionsToDelete);
    }

}
