package dev.bluemedia.timechamp.api.filter;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
import dev.bluemedia.timechamp.api.service.AuthenticationService;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.Session;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.util.ConfigUtil;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

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

    /** {@link HttpServletRequest} used to log the senders ip address if an authentication validation fails */
    @Context
    private HttpServletRequest sr;

    private String clientIp;

    /**
     * Filter method called by the Jersey Servlet Container when a matching request arrives.
     * @param context {@link ContainerRequestContext} used to access client cookies.
     */
    @Override
    public void filter(ContainerRequestContext context) {
        if (ConfigUtil.getConfig().isBehindReverseProxy()) {
            clientIp = sr.getHeader("X-Real-IP");
        } else {
            clientIp = sr.getRemoteAddr();
        }
        // Check if the client has sent a bearer authentication token and validate it
        try {
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

            Session trySession = AuthenticationService.validateSession(sessCookie.getValue(), clientIp);
            if (trySession != null) {
                context.setProperty("userFromFilter", trySession.getParentUser());
                context.setProperty("permission", trySession.getPermission());
            } else {
                abort(context, "Session cookie invalid or expired");
            }
        } catch (SQLException ex) {
            LOG.error("SQL error while validating authentication", ex);
            abort(context, "Failed to validate authentication");
        }
    }

    /**
     * Method called when the client is not authenticated or the authentication is invalid.
     * Rejects the request and sends the corresponding error to the client.
     * @param context {@link ContainerRequestContext} used to abort the request.
     */
    public void abort(ContainerRequestContext context, String reason) {
        LOG.warn("Rejected request from IP address {}. Reason: {}", clientIp, reason);
        context.abortWith(
                Response
                        .status(Response.Status.UNAUTHORIZED)
                        .cookie(new NewCookie.Builder("tsess").maxAge(0).build())
                        .entity("{\"error\":\"not_authenticated\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

}
