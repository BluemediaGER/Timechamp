package dev.bluemedia.timechamp.db;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.bluemedia.timechamp.db.dao.DbMetadataDaoImpl;
import dev.bluemedia.timechamp.model.object.DbMetadata;
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

    /**
     * Initialize database connections, tables and DAOs and start migrating the schema to the current version.
     * @param jdbcUrl JDBC URL used to connect to the database.
     */
    public static void init(String jdbcUrl) {
        try {
            connectionSource = new JdbcPooledConnectionSource(jdbcUrl);
            connectionSource.setMaxConnectionAgeMillis(5 * 60 * 1000);
            connectionSource.setTestBeforeGet(true);

            metadataDao = new DbMetadataDaoImpl(DaoManager.createDao(connectionSource, DbMetadata.class));
            TableUtils.createTableIfNotExists(connectionSource, DbMetadata.class);

            new MigrationHelper().migrate();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
            System.exit(1);
        }
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

}
