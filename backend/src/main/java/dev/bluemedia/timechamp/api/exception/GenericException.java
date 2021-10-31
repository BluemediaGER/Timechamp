package dev.bluemedia.timechamp.api.exception;

import dev.bluemedia.timechamp.model.response.GenericError;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Generic Exception for cases where a human-readable message should be included.
 *
 * @author Oliver Traber
 */
public class GenericException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = 4449670323669523644L;

    /**
     * ApplicationException thrown when an error occurs a human-readable message should be sent to the client.
     * @param error Machine readable error code.
     * @param message Error message containing further details for manual review.
     */
    public GenericException(Response.Status httpStatus, String error, String message) {
        super(Response
                .status(httpStatus)
                .entity(new GenericError(error, message))
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

}
