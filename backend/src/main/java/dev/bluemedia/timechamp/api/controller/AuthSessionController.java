package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.exception.NotFoundException;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.Session;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * REST controller to handle all session management related tasks.
 *
 * @author Oliver Traber
 */
@Path("/auth/session")
public class AuthSessionController {

    /** Injected {@link ContainerRequestContext} used to access identity information from filters */
    @Context
    private ContainerRequestContext context;

    /**
     * Get all sessions of the currently logged-in user.
     * @param managedUserId Optional user id to allow users with MANAGE privileges to impersonate other users.
     * @return List of {@link Session} object belonging to the currently logged-in or impersonated user.
     */
    @GET
    @RequireAuthentication
    @Produces(MediaType.APPLICATION_JSON)
    public List<Session> getSessions(@QueryParam("user") UUID managedUserId) {
        Permission requestPermission = (Permission) context.getProperty("permission");

        // Allow principals with MANAGE permission to impersonate other users
        User user = (User) context.getProperty("userFromFilter");
        if (requestPermission == Permission.MANAGE && managedUserId != null) {
            User managedUser = DBHelper.getUserDao().get(managedUserId);
            if (managedUser == null) {
                throw new NotFoundException("user_not_found");
            }
            user = managedUser;
        }

        return DBHelper.getSessionDao().getByParentUser(user);
    }

    /**
     * Delete all sessions of the currently logged-in user.
     * @param managedUserId Optional user id to allow users with MANAGE privileges to impersonate other users.
     * @return Empty array if all sessions were deleted.
     */
    @DELETE
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSessions(@QueryParam("user") UUID managedUserId) {
        Permission requestPermission = (Permission) context.getProperty("permission");

        // Allow principals with MANAGE permission to impersonate other users
        User user = (User) context.getProperty("userFromFilter");
        if (requestPermission == Permission.MANAGE && managedUserId != null) {
            User managedUser = DBHelper.getUserDao().get(managedUserId);
            if (managedUser == null) {
                throw new NotFoundException("user_not_found");
            }
            user = managedUser;
        }

        DBHelper.getSessionDao().removeAllSessionsOfUser(user);
        return Response.ok().entity("[]").build();
    }

    /**
     * Get a single session by its id.
     * @param sessionId Id of the session object that should be retrieved.
     * @return Session object from the database.
     */
    @GET
    @RequireAuthentication
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Session getSession(@PathParam("id") UUID sessionId) {
        Permission requestPermission = (Permission) context.getProperty("permission");
        User authenticatedUser = (User) context.getProperty("userFromFilter");

        Session session = DBHelper.getSessionDao().get(sessionId);
        if (session == null) throw new NotFoundException("session_not_found");

        // Allow principals with MANAGE permission to read sessions of other users
        if (!session.getParentUser().getId().equals(authenticatedUser.getId()) && requestPermission != Permission.MANAGE) {
            throw new NotFoundException("session_not_found");
        }

        return session;
    }

    /**
     * Delete a single session by its id.
     * @param sessionId Id of the session that should be deleted.
     * @return Empty array if the deletion was successful.
     */
    @DELETE
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSession(@PathParam("id") UUID sessionId) {
        Permission requestPermission = (Permission) context.getProperty("permission");
        User parentUser = (User) context.getProperty("userFromFilter");

        Session session = DBHelper.getSessionDao().get(sessionId);
        if (session == null) throw new NotFoundException("session_not_found");

        // Allow principals with MANAGE permission to delete sessions of other users
        if (!session.getParentUser().getId().equals(parentUser.getId()) && requestPermission != Permission.MANAGE) {
            throw new NotFoundException("session_not_found");
        }

        DBHelper.getSessionDao().delete(session);
        return Response.ok().entity("[]").build();
    }

}
