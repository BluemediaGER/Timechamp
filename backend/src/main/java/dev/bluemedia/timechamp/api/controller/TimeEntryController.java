package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.exception.BadRequestException;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.TimeEntry;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.type.Permission;
import dev.bluemedia.timechamp.model.type.TimeEntryType;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

/**
 * REST controller used to handle all TimeEntry related tasks.
 */
@Path("/time")
public class TimeEntryController {

    /** Injected {@link ContainerRequestContext} used to access identity information from filters */
    @Context
    private ContainerRequestContext context;

    @POST
    @Path("/start")
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Produces("application/json")
    public Response startNew() {
        User parentUser = (User) context.getProperty("userFromFilter");
        if (DBHelper.getTimeEntryDao().getActiveTimeEntry(parentUser) != null){
            throw new BadRequestException("has_active_time_entry");
        }
        TimeEntry timeEntry = new TimeEntry(parentUser, TimeEntryType.WORKTIME);
        timeEntry.setStartTime(LocalDateTime.now());
        DBHelper.getTimeEntryDao().persist(timeEntry);
        return Response.created(null).entity(timeEntry).build();
    }

    @POST
    @Path("/stop")
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Produces("application/json")
    public TimeEntry stopCurrent() {
        User parentUser = (User) context.getProperty("userFromFilter");
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
