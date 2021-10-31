package dev.bluemedia.timechamp.api.filter;

import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Request filter that checks if the user or API key has the permission to perform to execute an REST method,
 * when the REST method is annotated with the {@link RequirePermission} annotation.
 *
 * @author Oliver Traber
 */
@RequirePermission
@Provider
@Priority(Priorities.AUTHORIZATION)
public class PermissionFilter implements ContainerRequestFilter {

    /** {@link ResourceInfo} used to access the annotated method. */
    @Context
    private ResourceInfo resourceInfo;

    /**
     * Filter method called by the Jersey Servlet Container when a matching request arrives.
     * @param context {@link ContainerRequestContext} used to access client cookies and the authorization header.
     */
    @Override
    public void filter(ContainerRequestContext context) {
        // Get allowed permissions from annotation
        Method method = resourceInfo.getResourceMethod();
        if (method != null) {
            RequirePermission annotation = method.getAnnotation(RequirePermission.class);
            List<Permission> allowedPermissions = Arrays.asList(annotation.value());

            // Check if authentication uses an API key
            if (context.getProperty("apiKeyFromFilter") != null) {
                ApiKey key = (ApiKey) context.getProperty("apiKeyFromFilter");
                if (allowedPermissions.contains(key.getPermission())) return;
            }

            // Check if authentication uses a session
            if (context.getProperty("userFromFilter") != null) {
                User user = (User) context.getProperty("userFromFilter");
                if (allowedPermissions.contains(user.getPermission())) return;
            }

            // Fail permission check as default
            abort(context);
        }
    }

    /**
     * Method called when the client is not allowed to call an method.
     * Rejects the request and sends the corresponding error to the client.
     * @param context {@link ContainerRequestContext} used to abort the request.
     */
    private void abort(ContainerRequestContext context) {
        context.abortWith(
                Response
                        .status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"insufficient_permissions\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

}
