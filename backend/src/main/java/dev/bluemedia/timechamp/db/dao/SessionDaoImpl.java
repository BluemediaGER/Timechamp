package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO to access and manipulate {@link Session} objects.
 *
 * @author Oliver Traber
 */
public class SessionDaoImpl extends GenericDao<Session> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(SessionDaoImpl.class.getName());

    private final Map<String, Session> sessionCache = new HashMap<>();

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public SessionDaoImpl(Dao<Session, UUID> dao) {
        super(dao);
    }

    /**
     * Save a session to the database.
     * @param object Session that should be saved to the database.
     */
    @Override
    public void persist(Session object) {
        try {
            dao.create(object);
            sessionCache.put(object.getSessionKey(), object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Update a session in the database.
     * @param session Session that should be updated.
     */
    @Override
    public void update(Session session) {
        try {
            dao.update(session);
            sessionCache.put(session.getSessionKey(), session);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete a session from the database.
     * @param session Session that should be deleted from the database.
     */
    @Override
    public void delete(Session session) {
        super.delete(session);
        sessionCache.remove(session.getSessionKey());
    }

    /**
     * Get all {@link Session} objects that belong to the specified user.
     * @param parentUserId Id of the user whose keys should be retrieved.
     * @return List of {@link Session} objects which belong to the specified user.
     */
    public List<Session> getByParentUser(UUID parentUserId) {
        return getAllByAttributeMatch("parentUserId", parentUserId);
    }

    /**
     * Get a {@link Session} objects using its session key.
     * @param sessionKey Session key for which the session should be retrieved.
     * @return {@link Session} that matches the given session key, or null if no session with this key exists.
     */
    public Session getBySessionKey(String sessionKey) {
        if (sessionCache.containsKey(sessionKey)) {
            return sessionCache.get(sessionKey);
        }
        Session session = getByAttributeMatch("sessionKey", sessionKey);
        if (session != null) {
            sessionCache.put(session.getSessionKey(), session);
        }
        return session;
    }

    /**
     * Remove all {@link Session} objects that belong to the specified user.
     * @param parentUserId Id of the user whose keys should be removed.
     * @throws SQLException Exception thrown when an error occurs during deletion.
     */
    public void removeAllSessionsOfUser(UUID parentUserId) throws SQLException {
        List<Session> sessionsToDelete = getAllByAttributeMatch("parentUserId", parentUserId);
        dao.delete(sessionsToDelete);
        for (Session session : sessionsToDelete) {
            sessionCache.remove(session.getSessionKey());
        }
    }

}
