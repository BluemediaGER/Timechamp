package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.DbMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class DbMetadataDaoImpl extends GenericDao<DbMetadata> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(DbMetadataDaoImpl.class.getName());

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public DbMetadataDaoImpl(Dao<DbMetadata, String> dao) {
        super(dao);
    }

    public DbMetadata getLatest() {
        try {
            return dao.queryBuilder()
                    .orderBy("schemaVersion", false)
                    .limit(1L)
                    .query()
                    .get(0);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return null;
    }

    public void executeRawStatement(String statement) throws SQLException {
        dao.executeRawNoArgs(statement);
    }

}
