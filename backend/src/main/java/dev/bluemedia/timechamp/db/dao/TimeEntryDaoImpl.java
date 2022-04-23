package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.TimeEntry;
import dev.bluemedia.timechamp.model.object.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TimeEntryDaoImpl extends GenericDao<TimeEntry> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(TimeEntryDaoImpl.class.getName());

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public TimeEntryDaoImpl(Dao<TimeEntry, UUID> dao) {
        super(dao);
    }

    public TimeEntry getActiveTimeEntry(User user) {
        try {
            List<TimeEntry> results = dao.queryBuilder().where()
                    .eq("parentUser", user.getId())
                    .and()
                    .isNull("endTime")
                    .query();
            if (results.size() > 0) {
                return results.get(0);
            }
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return null;
    }

}
