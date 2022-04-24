package dev.bluemedia.timechamp.api.service;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import dev.bluemedia.timechamp.api.exception.BadRequestException;
import dev.bluemedia.timechamp.api.exception.GenericException;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.TimeEntry;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.TimeEntryRequest;
import dev.bluemedia.timechamp.model.type.TimeEntryType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Service used to manage all actions needed for TimeEntry management.
 *
 * @author Oliver Traber
 */
public class TimeEntryService {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(TimeEntryService.class.getName());

    public static TimeEntry createTimeEntry(User parentUser, TimeEntryRequest timeEntryRequest) {
        try {
            TimeEntry collision = DBHelper.getTimeEntryDao().getQueryBuilder().where()
                    .eq("parentUser", parentUser.getId()).and()
                    .between("startTime", timeEntryRequest.getStartTime(), timeEntryRequest.getEndTime()).or()
                    .between("endTime", timeEntryRequest.getStartTime(), timeEntryRequest.getEndTime())
                    .queryForFirst();
            if (collision != null) {
                throw new GenericException(Response.Status.NOT_ACCEPTABLE,
                        "time_entry_collision", "Time entry collides with another one");
            }
            TimeEntry timeEntry = new TimeEntry(parentUser, timeEntryRequest.getType());
            timeEntry.setStartTime(timeEntryRequest.getStartTime());
            timeEntry.setEndTime(timeEntryRequest.getEndTime());
            timeEntry.calculateWorktime();
            timeEntry.setDescription(timeEntryRequest.getDescription());
            DBHelper.getTimeEntryDao().persist(timeEntry);
            return timeEntry;
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR,
                    "database_error", "Error while creating time entry");
        }
    }

    public static List<TimeEntry> getTimeEntries(User user, String rawBefore) {
        QueryBuilder<TimeEntry, UUID> queryBuilder = DBHelper.getTimeEntryDao().getQueryBuilder();
        try {
            Where<TimeEntry, UUID> queryWhere = queryBuilder.limit(50L)
                    .orderBy("startTime", false).where()
                    .eq("parentUser", user.getId()).and()
                    .isNotNull("endTime");
            if (rawBefore != null) {
                LocalDateTime before = LocalDateTime.parse(rawBefore);
                queryWhere = queryWhere.and().lt("startTime", before);
            }
            return queryWhere.query();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR,
                    "database_error", "Error while querying time entries");
        } catch (DateTimeParseException ex) {
            throw new GenericException(Response.Status.BAD_REQUEST,
                    "invalid_date", "Invalid date format in 'before' query field");
        }
    }

    public static TimeEntry startNew(User parentUser) throws SQLException {
        if (DBHelper.getTimeEntryDao().getActiveTimeEntry(parentUser) != null){
            throw new BadRequestException("has_active_time_entry");
        }
        TimeEntry timeEntry = new TimeEntry(parentUser, TimeEntryType.WORKTIME);
        timeEntry.setStartTime(LocalDateTime.now());
        DBHelper.getTimeEntryDao().persist(timeEntry);
        return timeEntry;
    }

    public static TimeEntry endCurrent(User parentUser) throws SQLException {
        TimeEntry activeTimeEntry = DBHelper.getTimeEntryDao().getActiveTimeEntry(parentUser);
        if (activeTimeEntry == null){
            throw new BadRequestException("no_active_time_entry");
        }
        activeTimeEntry.setEndTime(LocalDateTime.now());
        activeTimeEntry.calculateWorktime();
        DBHelper.getTimeEntryDao().update(activeTimeEntry);
        return activeTimeEntry;
    }

}
