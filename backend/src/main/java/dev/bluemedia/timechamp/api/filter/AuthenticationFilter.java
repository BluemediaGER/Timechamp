package dev.bluemedia.timechamp.api.filter;

import dev.bluemedia.timechamp.api.anotation.RequireAuthentication;
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
        // TODO Implement authentication logic.
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
                        .entity("{\"error\":\"not_authenticated\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

}
