package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.service.AuthenticationService;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.ApiKeyCreateRequest;
import dev.bluemedia.timechamp.model.request.PermissionUpdateRequest;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST controller used to handle all API key related tasks.
 */
@Path("/auth/api-key")
public class AuthApiKeyController {

    /** Injected {@link ContainerRequestContext} used to access identity information from filters */
    @Context
    private ContainerRequestContext context;

    /**
     * Create a new {@link ApiKey} and store it in the database.
     * @return Created {@link ApiKey}.
     */
    @POST
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
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
     * @param managedUserId Optional user id to allow users with MANAGE privileges to impersonate other users.
     * @return List of {@link ApiKey} currently stored in the database.
     */
    @GET
    @RequireAuthentication
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiKey> getApiKeys(@QueryParam("user") String managedUserId) {
        Permission requestPermission = (Permission) context.getProperty("permission");
        User authenticatedUser = (User) context.getProperty("userFromFilter");

        // Allow principals with MANAGE permission to impersonate other users
        String userId = authenticatedUser.getId();
        if (requestPermission == Permission.MANAGE && managedUserId != null) {
            userId = managedUserId;
        }

        return DBHelper.getApiKeyDao().getByParentUser(userId);
    }

    /**
     * Get a single {@link ApiKey} by supplying its id.
     * @param apiKeyId Id of the {@link ApiKey} you want to get.
     * @return The {@link ApiKey} object that matches the given id.
     */
    @GET
    @RequireAuthentication
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiKey getApiKey(@NotEmpty @PathParam("id") String apiKeyId) {
        Permission requestPermission = (Permission) context.getProperty("permission");
        User parentUser = (User) context.getProperty("userFromFilter");

        ApiKey apiKey = DBHelper.getApiKeyDao().getByAttributeMatch("id", apiKeyId);
        if (apiKey == null) throw new NotFoundException("apikey_not_found");

        // Allow principals with MANAGE permission to read API keys of other users
        if (!apiKey.getParentUserId().equals(parentUser.getId()) && requestPermission != Permission.MANAGE) {
            throw new NotFoundException("apikey_not_found");
        }

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
    @Path("/{id}/secret")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiKeySecret(@NotNull @PathParam("id") String keyId) {
        Permission requestPermission = (Permission) context.getProperty("permission");
        ApiKey key = getApiKey(keyId);

        // Prevent API key with READ_WRITE permission from reading secrets of keys with MANAGE permission
        if (requestPermission == Permission.READ_WRITE && key.getPermission() == Permission.MANAGE) {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"insufficient_permissions\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response
                .ok()
                .entity("{\"secret\":\"" + key.getAuthenticationKey() + "\"}")
                .build();
    }

    /**
     * Update the {@link Permission} of an existing {@link ApiKey}.
     * @param authorizationHeader Authorization header used to prevent an {@link ApiKey}
     *                            from changing its own permission.
     * @param keyId Id of the {@link ApiKey} the {@link Permission} should be updated for.
     * @param permissionUpdateRequest {@link PermissionUpdateRequest} containing the new {@link Permission}.
     * @return Updated {@link ApiKey} if the operation was successful.
     */
    @PUT
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/permission")
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
        if (key == null) throw new dev.bluemedia.timechamp.api.exception.NotFoundException("apikey_not_found");
        if (!key.getParentUserId().equals(parentUser.getId())) throw new dev.bluemedia.timechamp.api.exception.NotFoundException("apikey_not_found");
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
     * @param apiKeyId Id of the {@link ApiKey} that should be deleted.
     */
    @DELETE
    @RequireAuthentication
    @RequirePermission({Permission.READ_WRITE, Permission.MANAGE})
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApiKey(@NotNull @PathParam("id") String apiKeyId) {
        Permission requestPermission = (Permission) context.getProperty("permission");
        User parentUser = (User) context.getProperty("userFromFilter");

        ApiKey apiKey = DBHelper.getApiKeyDao().getByAttributeMatch("id", apiKeyId);
        if (apiKey == null) throw new NotFoundException("apikey_not_found");

        // Allow principals with MANAGE permission to delete API keys of other users
        if (!apiKey.getParentUserId().equals(parentUser.getId()) && requestPermission != Permission.MANAGE) {
            throw new NotFoundException("apikey_not_found");
        }

        DBHelper.getApiKeyDao().delete(apiKey);
        return Response.ok().entity("[]").build();
    }

}
