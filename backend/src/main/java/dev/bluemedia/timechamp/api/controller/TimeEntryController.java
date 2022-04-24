package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.exception.NotFoundException;
import dev.bluemedia.timechamp.api.service.TimeEntryService;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.TimeEntry;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.TimeEntryRequest;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller used to handle all TimeEntry related tasks.
 */
@Path("/time")
public class TimeEntryController {

    @Inject
    private Provider<User> contextUser;

    @GET
    @RequireAuthentication
    @Produces("application/json")
    public List<TimeEntry> getTimeEntries(@QueryParam("before") String rawBefore) {
        return TimeEntryService.getTimeEntries(contextUser.get(), rawBefore);
    }

    @POST
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Consumes("application/json")
    @Produces("application/json")
    public TimeEntry createTimeEntry(@Valid TimeEntryRequest timeEntryRequest) {
        return TimeEntryService.createTimeEntry(contextUser.get(), timeEntryRequest);
    }

    @GET
    @Path("/current")
    @RequireAuthentication
    @Produces("application/json")
    public TimeEntry getCurrentActive() throws SQLException {
        TimeEntry currentTimeEntry = DBHelper.getTimeEntryDao().getActiveTimeEntry(contextUser.get());
        if (currentTimeEntry != null) {
            currentTimeEntry.setWorktime(Duration.between(currentTimeEntry.getStartTime(), LocalDateTime.now()));
            return currentTimeEntry;
        }
        throw new NotFoundException("no_active_time_entry");
    }

    @GET
    @Path("/{id}")
    @RequireAuthentication
    @Produces("application/json")
    public TimeEntry getTimeEntry(@PathParam("id") UUID id) throws SQLException {
        TimeEntry timeEntry = DBHelper.getTimeEntryDao().get(contextUser.get(), id);
        if (timeEntry == null) {
            throw new NotFoundException("time_entry_not_found");
        }
        return timeEntry;
    }

    @DELETE
    @Path("/{id}")
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Produces("application/json")
    public Response deleteTimeEntry(@PathParam("id") UUID id) throws SQLException {
        TimeEntry timeEntry = DBHelper.getTimeEntryDao().get(contextUser.get(), id);
        if (timeEntry == null) {
            throw new NotFoundException("time_entry_not_found");
        }
        DBHelper.getTimeEntryDao().delete(timeEntry);
        return Response.ok("[]").build();
    }

    @POST
    @Path("/start")
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Produces("application/json")
    public Response startNew() throws SQLException {
        TimeEntry timeEntry = TimeEntryService.startNew(contextUser.get());
        return Response.created(null).entity(timeEntry).build();
    }

    @POST
    @Path("/end")
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Produces("application/json")
    public TimeEntry stopCurrent() throws SQLException {
        return TimeEntryService.endCurrent(contextUser.get());
    }
}
