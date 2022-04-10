package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.exception.NotFoundException;
import dev.bluemedia.timechamp.api.service.AuthenticationService;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.PasswordUpdateRequest;
import dev.bluemedia.timechamp.model.request.PermissionUpdateRequest;
import dev.bluemedia.timechamp.model.request.UserCreateRequest;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * REST controller used to handle all tasks related to user management.
 *
 * @author Oliver Traber
 */
@Path("/user")
public class UserController {

    /** Injected {@link ContainerRequestContext} used to access identity information from filters */
    @Context
    private ContainerRequestContext context;

    /**
     * Create a new {@link User} in the database.
     * @param request {@link UserCreateRequest} containing the details for the new user.
     * @return Created {@link User} if the operation was successful.
     */
    @POST
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@Valid UserCreateRequest request) {
        User user = AuthenticationService.createUser(request);
        return Response.created(null).entity(user).build();
    }

    /**
     * Get the list of all {@link User} currently stored in the database.
     * @return {@link List} containing all {@link User} currently stored in the database.
     */
    @GET
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers() {
        return DBHelper.getUserDao().getAll();
    }

    /**
     * Get a specific {@link User} by its id.
     * @param userId Id of the {@link User} that should be retrieved.
     * @return {@link User} matching the given id.
     */
    @GET
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") UUID userId) {
        User user = DBHelper.getUserDao().get(userId);
        if (user == null) throw new NotFoundException("user_not_found");
        return user;
    }

    /**
     * Update the password of an {@link User} based on its id.
     * @param userId Id of the {@link User} that should be updated.
     * @param passwordUpdateRequest {@link PasswordUpdateRequest} containing the new password for the user.
     * @return Updated {@link User} if the operation was successful.
     */
    @PUT
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Path("/{id}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUserPassword(@PathParam("id") UUID userId,
                                   @Valid PasswordUpdateRequest passwordUpdateRequest) {
        return AuthenticationService.updateUserPasswordById(userId, passwordUpdateRequest.getPassword());
    }

    /**
     * Update the {@link Permission} for an {@link User} based on its id.
     * @param userId Id of the {@link User} that should be updated.
     * @param permissionUpdateRequest {@link PermissionUpdateRequest} containing the new {@link Permission}.
     * @return Updated {@link User} if the operation was successful.
     */
    @PUT
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUserPermission(@PathParam("id") UUID userId,
                                     @Valid PermissionUpdateRequest permissionUpdateRequest) {
        User authenticatedUser = (User) context.getProperty("userFromFilter");
        // Prevent users from changing their own permissions
        if (authenticatedUser.getId().equals(userId)) {
            throw new BadRequestException("cant_change_own_permission");
        }
        User user = DBHelper.getUserDao().get(userId);
        if (user == null) throw new NotFoundException("user_not_found");
        user.updatePermission(permissionUpdateRequest.getPermission());
        DBHelper.getSessionDao().removeAllSessionsOfUser(user);
        DBHelper.getUserDao().update(user);
        return user;
    }

    /**
     * Delete an {@link User} from the database.
     * @param userId Id of the {@link User} that should be deleted.
     * @return Empty array and status 200 if the operation was successful.
     */
    @DELETE
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") UUID userId) {
        User authenticatedUser = (User) context.getProperty("userFromFilter");
        return AuthenticationService.deleteUser(authenticatedUser, userId);
    }

}
