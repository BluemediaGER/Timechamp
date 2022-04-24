package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericDao<T> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(GenericDao.class.getName());

    /** {@link Dao} that should be used for database operations */
    protected Dao<T, UUID> dao;

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public GenericDao(Dao<T, UUID> dao) {
        this.dao = dao;
    }

    /**
     * Save an object to the database.
     * @param object Object that should be saved to the database.
     */
    public void persist(T object) throws SQLException {
        try {
            dao.create(object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Update an object in the database.
     * @param object Object that should be updated.
     */
    public void update(T object) throws SQLException {
        try {
            dao.update(object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete an object from the database.
     * @param object Object that should be deleted from the database.
     */
    public void delete(T object) {
        try {
            dao.delete(object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Get an object from the database using its ID.
     * @param id ID of the object that should be retrieved.
     * @return Object that was retrieved from the database, or null if no object could be found.
     */
    public T get(UUID id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
            return null;
        }
    }

    /**
     * Retrieve all objects that are contained in the database.
     * @return List of objects that are currently stored in the database.
     */
    public List<T> getAll() {
        List<T> list = new ArrayList<>();
        try {
            list = dao.queryForAll();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return list;
    }

    /**
     * Retrieve an single object from the local database using any attribute.
     * @return Instance of the found, or null if no entry could be found.
     */
    public <E> T getByAttributeMatch(String attributeName, E attributeValue) throws SQLException {
        QueryBuilder<T, UUID> queryBuilder = dao.queryBuilder();
        queryBuilder.where().eq(attributeName, attributeValue);
        List<T> results = dao.query(queryBuilder.prepare());
        if (results.size() == 0) {
            return null;
        } else {
            return results.get(0);
        }
    }

    /**
     * Retrieve all matching objects from the local database using any attribute.
     * @return Instance of the found, or null if no entry could be found.
     */
    public <E> List<T> getAllByAttributeMatch(String attributeName, E attributeValue) throws SQLException {
        QueryBuilder<T, UUID> queryBuilder = dao.queryBuilder();
        queryBuilder.where().eq(attributeName, attributeValue);
        List<T> results = dao.query(queryBuilder.prepare());
        if (results.size() == 0) {
            return new ArrayList<>();
        } else {
            return results;
        }
    }

    /**
     * Get an QueryBuilder instance from the DAO.
     * @return QueryBuilder instance from the DAO.
     */
    public QueryBuilder<T, UUID> getQueryBuilder() {
        return dao.queryBuilder();
    }

    /**
     * Get a list of results matching the given query.
     * @param queryBuilder Query the found objects must match.
     * @return List of results matching the given query.
     */
    public List<T> query(QueryBuilder<T, UUID> queryBuilder) throws SQLException {
        return dao.query(queryBuilder.prepare());
    }

    /**
     * Fill missing fields in lazy loaded objects.
     * @param object Object that should be filled.
     * @return Object with filled values.
     */
    public T refresh(T object) throws SQLException {
        dao.refresh(object);
        return object;
    }

    /**
     * Get the count of all objects currently persisted in the database.
     * @return Count of all objects currently persisted in the database.
     */
    public long countOf() throws SQLException {
        return dao.countOf();
    }

}
