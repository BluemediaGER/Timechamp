package dev.bluemedia.timechamp.api.controller;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.anotation.RequirePermission;
import dev.bluemedia.timechamp.api.exception.GenericException;
import dev.bluemedia.timechamp.api.service.AuthenticationService;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.PasswordUpdateRequest;
import dev.bluemedia.timechamp.model.type.Permission;
import dev.bluemedia.timechamp.util.ConfigUtil;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * REST controller used to handle all related tasks related to authentication.
 *
 * @author Oliver Traber
 */
@Path("/auth")
public class AuthController {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class.getName());

    /** Injected {@link HttpServletRequest} used to log the clients ip address in case the login fails */
    @Context
    private HttpServletRequest sr;

    /** Injected {@link ContainerRequestContext} used to access identity information from filters */
    @Context
    private ContainerRequestContext context;

    @Inject
    private Provider<User> contextUser;

    @Inject
    private Provider<Permission> contextPermission;

    /**
     * Method used by a frontend to obtain a new session.
     * @param password Password send by the client.
     * @return Returns the {@link User} in case the login is successful, or an error if it fails.
     */
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response login(@FormDataParam("username") String username, @FormDataParam("password") String password) throws SQLException {
        String clientIp = sr.getRemoteAddr();
        if (ConfigUtil.getConfig().isBehindReverseProxy()) {
            clientIp = sr.getHeader("X-Real-IP");
        }
        User user = AuthenticationService.validateCredentials(username, password);
        if (user != null) {
            String sessionKey = AuthenticationService.issueSession(username, sr.getHeader("User-Agent"), clientIp);
            try {
                return Response
                        .ok()
                        .entity(user)
                        .cookie(
                                new NewCookie.Builder("tsess")
                                        .value(sessionKey)
                                        .expiry(Date.from(Instant.now().plus(90, ChronoUnit.DAYS)))
                                        .build()
                        ).build();
            } catch (Exception ex) {
                LOG.error("Failed to create session cookie for user {}", username, ex);
                throw new GenericException(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        "internal_server_error",
                        "create_session_failed"
                );
            }
        }

        LOG.warn("Login failed from IP {} using username {}. Reason: Invalid credentials", clientIp, username);
        return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\":\"invalid_credentials\"}")
                .build();
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
    public Response logout(@CookieParam("tsess") Cookie sessionCookie) throws SQLException {
        if (sessionCookie != null) AuthenticationService.invalidateSession(sessionCookie.getValue());
        return Response
                .ok()
                .entity("[]")
                .cookie(new NewCookie.Builder("tsess").maxAge(0).build())
                .build();
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
    public Response updateUserPassword(@Valid PasswordUpdateRequest passwordUpdateRequest) throws SQLException {
        User user = contextUser.get();
        DBHelper.getUserDao().refresh(user);
        // Prevent API key with lower privileges from resetting the password of a user with higher privileges.
        ApiKey apiKey = (ApiKey) context.getProperty("apiKeyFromFilter");
        if (apiKey != null) {
            if (user.getPermission() == Permission.MANAGE && apiKey.getPermission() != Permission.MANAGE) {
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"insufficient_permissions\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        }
        return Response
                .ok(AuthenticationService.updateUserPassword(user, passwordUpdateRequest.getPassword()))
                .build();
    }

}
