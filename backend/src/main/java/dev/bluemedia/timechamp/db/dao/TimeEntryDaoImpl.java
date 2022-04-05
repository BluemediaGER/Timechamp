package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.TimeEntry;

public class TimeEntryDaoImpl extends GenericDao<TimeEntry> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public TimeEntryDaoImpl(Dao<TimeEntry, String> dao) {
        super(dao);
    }
}
