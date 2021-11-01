package dev.bluemedia.timechamp.api.filter;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.service.AuthenticationService;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.Session;
import dev.bluemedia.timechamp.model.object.User;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request filter that checks if the user is authenticated,
 * when an REST method is annotated with the {@link RequireAuthentication} annotation.
 *
 * @author Oliver Traber
 */
@Provider
@RequireAuthentication
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class.getName());

    /** {@link HttpServletRequest} used to log the senders ip address if a authentication validation fails */
    @Context
    private HttpServletRequest sr;

    /**
     * Filter method called by the Jersey Servlet Container when a matching request arrives.
     * @param context {@link ContainerRequestContext} used to access client cookies.
     */
    @Override
    public void filter(ContainerRequestContext context) {
        // Check if the client has sent a bearer authentication token and validate it
        if (context.getHeaderString("X-API-Key") != null) {
            ApiKey tryKey = AuthenticationService.validateApiKey(context.getHeaderString("X-API-Key"));
            if (tryKey != null) {
                context.setProperty("userFromFilter", tryKey.getParentUser());
                context.setProperty("apiKeyFromFilter", tryKey);
                context.setProperty("permission", tryKey.getPermission());
            } else {
                abort(context, "API key invalid");
            }
            return;
        }

        Cookie sessCookie = null;
        if (context.getCookies().containsKey("tsess")) {
            sessCookie = context.getCookies().get("tsess");
        }

        if (sessCookie == null) {
            abort(context, "Session cookie not set");
            return;
        }

        Session trySession = AuthenticationService.validateSession(sessCookie.getValue());
        if (trySession != null) {
            User user = trySession.getParentUser();
            context.setProperty("userFromFilter", user);
            context.setProperty("permission", user.getPermission());
        } else {
            abort(context, "Session cookie invalid or expired");
        }
    }

    /**
     * Method called when the client is not authenticated or the authentication is invalid.
     * Rejects the request and sends the corresponding error to the client.
     * @param context {@link ContainerRequestContext} used to abort the request.
     */
    public void abort(ContainerRequestContext context, String reason) {
        LOG.warn("Rejected request from IP address {}. Reason: {}", sr.getRemoteAddr(), reason);
        context.abortWith(
                Response
                        .status(Response.Status.UNAUTHORIZED)
                        .cookie(
                                new NewCookie(
                                        new Cookie("tsess",
                                                "",
                                                "/",
                                                context.getHeaderString("host")
                                                        .substring(0, context
                                                                .getHeaderString("host")
                                                                .lastIndexOf(':')
                                                        )
                                        )
                                )
                        )
                        .entity("{\"error\":\"not_authenticated\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

}
