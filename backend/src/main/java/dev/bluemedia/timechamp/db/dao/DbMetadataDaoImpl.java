package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.DbMetadata;

import java.sql.SQLException;
import java.util.UUID;

/**
 * DAO to access and manipulate {@link DbMetadata} objects.
 *
 * @author Oliver Traber
 */
public class DbMetadataDaoImpl extends GenericDao<DbMetadata> {

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public DbMetadataDaoImpl(Dao<DbMetadata, UUID> dao) {
        super(dao);
    }

    public DbMetadata getLatest() throws SQLException {
        return dao.queryBuilder()
                .orderBy("schemaVersion", false)
                .limit(1L)
                .query()
                .get(0);
    }

    /**
     * Execute a raw SQL statement on the database. Used by the {@link dev.bluemedia.timechamp.db.MigrationHelper}.
     * @param statement SQL statement that should be executed.
     * @throws SQLException Exception thrown if any error occurs during statement execution.
     */
    public void executeRawStatement(String statement) throws SQLException {
        dao.executeRawNoArgs(statement);
    }

}
