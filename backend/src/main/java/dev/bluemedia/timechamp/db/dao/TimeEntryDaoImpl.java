package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.TimeEntry;

import java.util.UUID;

public class TimeEntryDaoImpl extends GenericDao<TimeEntry> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public TimeEntryDaoImpl(Dao<TimeEntry, UUID> dao) {
        super(dao);
    }
}
