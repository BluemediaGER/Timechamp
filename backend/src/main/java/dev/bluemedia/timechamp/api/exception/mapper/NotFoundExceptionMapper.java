package dev.bluemedia.timechamp.api.exception.mapper;

import dev.bluemedia.timechamp.model.response.GenericError;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * {@link ExceptionMapper} implementation used to map a {@link NotFoundException} to a json response.
 *
 * @author Oliver Traber
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    /**
     * Map a given {@link NotFoundException} to a response.
     * @param ex {@link NotFoundException} that should be mapped.
     * @return {@link Response} containing the json error object.
     */
    @Override
    public Response toResponse(NotFoundException ex) {
        GenericError error = new GenericError();
        error.error = "not_found";
        error.message = "The given path could not be found by the server.";
        return Response
                .status(404)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

}