package dev.bluemedia.timechamp.api.exception.mapper;

import dev.bluemedia.timechamp.model.response.GenericError;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {@link ExceptionMapper} implementation used to map any {@link Exception} to a response.
 *
 * @author Oliver Traber
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class.getName());

    /**
     * Map an given {@link Exception} to a response.
     * @param ex {@link Exception} that should be mapped.
     * @return {@link Response} containing the stacktrace.
     */
    @Override
    public Response toResponse(Exception ex) {
        GenericError error = new GenericError();
        if (ex instanceof WebApplicationException) {
            WebApplicationException webAppEx = (WebApplicationException) ex;
            error.error = webAppEx.getResponse().getStatusInfo().getReasonPhrase();
            error.message = ex.getMessage();
            if (webAppEx.getResponse().getStatusInfo().getFamily() != Response.Status.Family.CLIENT_ERROR) {
                LOG.error("An unexpected error occurred", ex);
            }
            return Response
                    .status(((WebApplicationException) ex).getResponse().getStatus())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        } else {
            LOG.error("An unexpected error occurred", ex);
            error.error = "internal_error";
            error.message = ex.getMessage();
            return Response
                    .status(500)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }
    }

}