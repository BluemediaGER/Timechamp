package dev.bluemedia.timechamp.db;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.bluemedia.timechamp.db.dao.ApiKeyDaoImpl;
import dev.bluemedia.timechamp.db.dao.DbMetadataDaoImpl;
import dev.bluemedia.timechamp.db.dao.SessionDaoImpl;
import dev.bluemedia.timechamp.db.dao.UserDaoImpl;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.DbMetadata;
import dev.bluemedia.timechamp.model.object.Session;
import dev.bluemedia.timechamp.model.object.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Class for access to and management of the database.
 *
 * @author Oliver Traber
 */
public class DBHelper {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(DBHelper.class.getName());

    /** Connection source for the persistent database */
    private static JdbcPooledConnectionSource connectionSource;

    /** {@link DbMetadataDaoImpl} used to persist {@link DbMetadata} objects to the database */
    private static DbMetadataDaoImpl metadataDao;

    /** {@link UserDaoImpl} used to persist {@link User} objects to the database */
    private static UserDaoImpl userDao;

    /** {@link ApiKeyDaoImpl} used to persist {@link ApiKey} objects to the database */
    private static ApiKeyDaoImpl apiKeyDao;

    /** {@link SessionDaoImpl} used to persist {@link Session} objects to the database */
    private static SessionDaoImpl sessionDao;

    /**
     * Initialize database connections, tables and DAOs and start migrating the schema to the current version.
     * @param jdbcUrl JDBC URL used to connect to the database.
     */
    public static void init(String jdbcUrl) {
        try {
            connectionSource = new JdbcPooledConnectionSource(jdbcUrl);
            connectionSource.setMaxConnectionAgeMillis(5 * 60 * 1000);
            connectionSource.setTestBeforeGet(true);

            initDAOs(connectionSource);
            initTables(connectionSource);

            new MigrationHelper().migrate();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
            System.exit(1);
        }
    }

    /**
     * Method to initialize all DAOs used to access the database.
     * @param connectionSource SQL connection source used to communicate with the database.
     * @throws SQLException Exception thrown if any errors occur during DAO initialisation.
     */
    private static void initDAOs(ConnectionSource connectionSource) throws SQLException {
        metadataDao = new DbMetadataDaoImpl(DaoManager.createDao(connectionSource, DbMetadata.class));
        userDao = new UserDaoImpl(DaoManager.createDao(connectionSource, User.class));
        apiKeyDao = new ApiKeyDaoImpl(DaoManager.createDao(connectionSource, ApiKey.class));
        sessionDao = new SessionDaoImpl(DaoManager.createDao(connectionSource, Session.class));
    }

    /**
     * Method to initialize all database tables used to store objects.
     * @param connectionSource SQL connection source used to communicate with the database.
     * @throws SQLException Exception thrown if any errors occur during table initialisation.
     */
    private static void initTables(ConnectionSource connectionSource) throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, DbMetadata.class);
        TableUtils.createTableIfNotExists(connectionSource, User.class);
        TableUtils.createTableIfNotExists(connectionSource, ApiKey.class);
        TableUtils.createTableIfNotExists(connectionSource, Session.class);
    }

    /** Close the database connections */
    public static void close() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
            }
        } catch (IOException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Get the {@link DbMetadataDaoImpl} used to persist {@link DbMetadata} objects to the database.
     * @return {@link DbMetadataDaoImpl} used to persist {@link DbMetadata} objects to the database.
     */
    protected static DbMetadataDaoImpl getMetadataDao() {
        return metadataDao;
    }

    /**
     * Get the {@link UserDaoImpl} used to persist {@link User} objects to the database.
     * @return {@link UserDaoImpl} used to persist {@link User} objects to the database.
     */
    public static UserDaoImpl getUserDao() {
        return userDao;
    }

    /**
     * Get the {@link ApiKeyDaoImpl} used to persist {@link ApiKey} objects to the database.
     * @return {@link ApiKeyDaoImpl} used to persist {@link ApiKey} objects to the database.
     */
    public static ApiKeyDaoImpl getApiKeyDao() {
        return apiKeyDao;
    }

    /**
     * Get the {@link SessionDaoImpl} used to persist {@link Session} objects to the database.
     * @return {@link SessionDaoImpl} used to persist {@link Session} objects to the database.
     */
    public static SessionDaoImpl getSessionDao() {
        return sessionDao;
    }
}
