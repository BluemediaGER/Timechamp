package dev.bluemedia.timechamp.api.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * ApplicationException thrown when a requested object is not existent.
 *
 * @author Oliver Traber
 */
public class NotFoundException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = -3421690330574443604L;

    /**
     * ApplicationException thrown when a requested object is not existent.
     * @param errorMessage Error message that should be embedded in the error json response.
     */
    public NotFoundException(String errorMessage) {
        super(Response
                .status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"" + errorMessage + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

}
