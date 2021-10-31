package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.service.AuthenticationService;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.ApiKeyCreateRequest;
import dev.bluemedia.timechamp.model.request.PasswordUpdateRequest;
import dev.bluemedia.timechamp.model.request.PermissionUpdateRequest;
import dev.bluemedia.timechamp.model.request.UserCreateRequest;
import dev.bluemedia.timechamp.model.type.Permission;
import dev.bluemedia.timechamp.util.ConfigUtil;
import dev.bluemedia.timechamp.api.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * REST controller used to handle all authentication and identity related actions.
 *
 * @author Oliver Traber
 */
@Path("/auth")
public class AuthenticationController {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class.getName());

    /** Injected {@link HttpHeaders} used to get the hostname used to set cookies on the client */
    @Context
    private HttpHeaders httpHeaders;

    /** Injected {@link HttpServletRequest} used to log the clients ip address in case the login fails */
    @Context
    private HttpServletRequest sr;

    /** Injected {@link ContainerRequestContext} used to access identity information from filters */
    @Context
    private ContainerRequestContext context;

    /**
     * Method used by a frontend to obtain a new session.
     * @param password Password send by the client.
     * @return Returns the {@link User} in case the login is successful, or an error if it fails.
     */
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response login(@FormDataParam("username") String username, @FormDataParam("password") String password) {
        String clientIp = sr.getRemoteAddr();
        if (ConfigUtil.getConfig().isBehindReverseProxy()) {
            clientIp = sr.getHeader("X-Real-IP");
        }
        if (!AuthenticationService.validateCredentials(username, password)) {
            LOG.warn("Login failed from IP {} using username {}. Reason: Invalid credentials", clientIp, username);
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"invalid_credentials\"}")
                    .build();
        }
        String sessionKey = AuthenticationService.issueSession(username, sr.getHeader("User-Agent"), clientIp);
        try {
            String cookieHostname = httpHeaders.getRequestHeader("host").get(0);
            // Remove port from host header
            final Pattern portPattern = Pattern.compile(":[0-9]+");
            cookieHostname = portPattern.matcher(cookieHostname).replaceAll("");

            return Response
                    .ok()
                    .entity(DBHelper.getUserDao().getByAttributeMatch("username", username))
                    .cookie(new NewCookie(new Cookie("tsess", sessionKey, "/", cookieHostname)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method used by a frontend to invalidate an existing session.
     * @param sessionCookie Session cookie sent by the client.
     * @return Returns an empty array and http status 200 in all cases. Sets an empty session cookie on the client.
     */
    @GET
    @RequireAuthentication
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam("tsess") Cookie sessionCookie) {
        if (sessionCookie != null) AuthenticationService.invalidateSession(sessionCookie.getValue());
        String cookieHostname = httpHeaders.getRequestHeader("host").get(0);
        // Remove port from host header
        final Pattern portPattern = Pattern.compile(":[0-9]+");
        cookieHostname = portPattern.matcher(cookieHostname).replaceAll("");
        return Response
                .ok()
                .entity("[]")
                .cookie(new NewCookie(new Cookie("tsess", "", "/", cookieHostname)))
                .build();
    }

    /**
     * Create a new {@link ApiKey} and store it in the database.
     * @return Created {@link ApiKey}.
     */
    @POST
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Path("/api-key")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApiKey(@Valid ApiKeyCreateRequest createRequest) {
        User parentUser = (User) context.getProperty("userFromFilter");
        // Don't allow users to create API keys with MANAGE permissions,
        // if they do not have MANAGE permissions themselves.
        if (createRequest.getPermission() == Permission.MANAGE && parentUser.getPermission() != Permission.MANAGE) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"insufficient_permissions\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        ApiKey key = AuthenticationService.createApiKey(parentUser.getId(), createRequest);
        return Response.created(null).entity(key).build();
    }

    /**
     * Get a list of {@link ApiKey} currently stored in the database.
     * @return List of {@link ApiKey} currently stored in the database.
     */
    @GET
    @RequireAuthentication
    @Path("/api-key")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiKey> getApiKeys() {
        User parentUser = (User) context.getProperty("userFromFilter");
        return DBHelper.getApiKeyDao().getByParentUser(parentUser.getId());
    }

    /**
     * Get a single {@link ApiKey} by supplying it's id.
     * @param apiKeyId Id of the {@link ApiKey} you want to get.
     * @return The {@link ApiKey} object that matches the given id.
     */
    @GET
    @RequireAuthentication
    @Path("/api-key/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiKey getApiKey(@NotEmpty @PathParam("id") String apiKeyId) {
        User parentUser = (User) context.getProperty("userFromFilter");
        ApiKey apiKey = DBHelper.getApiKeyDao().getByAttributeMatch("id", apiKeyId);
        if (apiKey == null) throw new NotFoundException("apikey_not_existing");
        if (!apiKey.getParentUserId().equals(parentUser.getId())) throw new NotFoundException("apikey_not_existing");
        return apiKey;
    }

    /**
     * Get the secret from the {@link ApiKey} using the given id.
     * @param keyId Id of the {@link ApiKey} the secret should be got from.
     * @return Secret corresponding to the given {@link ApiKey}.
     */
    @GET
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Path("/api-key/{id}/secret")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiKeySecret(@NotNull @PathParam("id") String keyId) {
        ApiKey key = getApiKey(keyId);
        return Response
                .ok()
                .entity("{\"secret\":\"" + key.getAuthenticationKey() + "\"}")
                .build();
    }

    /**
     * Update the {@link Permission} of an existing {@link ApiKey}.
     * @param authorizationHeader Authorization header used to prevent an {@link ApiKey}
     *                            from changing it's own permission.
     * @param keyId Id of the {@link ApiKey} the {@link Permission} should be updated for.
     * @param permissionUpdateRequest {@link PermissionUpdateRequest} containing the new {@link Permission}.
     * @return Updated {@link ApiKey} if the operation was successful.
     */
    @PUT
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/api-key/{id}/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApiKeyPermission(@HeaderParam("X-API-Key") String authorizationHeader,
                                         @PathParam("id") String keyId,
                                         @Valid PermissionUpdateRequest permissionUpdateRequest) {
        User parentUser = (User) context.getProperty("userFromFilter");
        // Prevent api key from changing its own permission
        if(authorizationHeader != null) {
            ApiKey requestingKey = AuthenticationService.getApiKey(authorizationHeader);
            if (keyId.equals(requestingKey.getId())) {
                throw new BadRequestException("cant_change_own_permission");
            }
        }
        ApiKey key = DBHelper.getApiKeyDao().getByAttributeMatch("id", keyId);
        if (key == null) throw new NotFoundException("apikey_not_existing");
        if (!key.getParentUserId().equals(parentUser.getId())) throw new NotFoundException("apikey_not_existing");
        // Don't allow users to create API keys with MANAGE permissions,
        // if they do not have MANAGE permissions themselves.
        if (permissionUpdateRequest.getPermission() == Permission.MANAGE &&
                parentUser.getPermission() != Permission.MANAGE) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"insufficient_permissions\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        key.updatePermission(permissionUpdateRequest.getPermission());
        DBHelper.getApiKeyDao().update(key);
        return Response
                .ok()
                .entity(key)
                .build();
    }

    /**
     * Delete the {@link ApiKey} with the given id from the database.
     * @param keyId Id of the {@link ApiKey} that should be deleted.
     */
    @DELETE
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Path("/api-key/{key_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApiKey(@NotNull @PathParam("key_id") String keyId) {
        AuthenticationService.deleteApiKey(keyId);
        return Response
                .ok()
                .entity("[]")
                .build();
    }

    /**
     * Create an new {@link User} in the database.
     * @param request {@link UserCreateRequest} containing the details for the new user.
     * @return Created {@link User} if the operation was successful.
     */
    @POST
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Path("/user")
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
    @Path("/user")
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
    @Path("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") String userId) {
        User user = DBHelper.getUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_found");
        return user;
    }

    /**
     * Update the password of the currently logged in {@link User}.
     * @param passwordUpdateRequest {@link PasswordUpdateRequest} containing the new password for the user.
     * @return Updated {@link User} if the operation was successful.
     */
    @PUT
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Path("/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUserPassword(@Valid PasswordUpdateRequest passwordUpdateRequest) {
        User user = (User) context.getProperty("userFromFilter");
        return AuthenticationService.updateUserPassword(user, passwordUpdateRequest.getPassword());
    }

    /**
     * Update the password of an {@link User} based on it's id.
     * @param userId Id of the {@link User} that should be updated.
     * @param passwordUpdateRequest {@link PasswordUpdateRequest} containing the new password for the user.
     * @return Updated {@link User} if the operation was successful.
     */
    @PUT
    @RequireAuthentication
    @RequirePermission(Permission.MANAGE)
    @Path("/user/{id}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUserPassword(@PathParam("id") String userId,
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
    @Path("/user/{id}/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUserPermission(@PathParam("id") String userId,
                                     @Valid PermissionUpdateRequest permissionUpdateRequest) {
        User authenticatedUser = (User) context.getProperty("userFromFilter");
        // Prevent users from changing their own permissions
        if (authenticatedUser.getId().equals(userId)) {
            throw new BadRequestException("cant_change_own_permission");
        }
        User user = DBHelper.getUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        user.updatePermission(permissionUpdateRequest.getPermission());
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
    @Path("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") String userId) {
        User authenticatedUser = (User) context.getProperty("userFromFilter");
        return AuthenticationService.deleteUser(authenticatedUser, userId);
    }

}
