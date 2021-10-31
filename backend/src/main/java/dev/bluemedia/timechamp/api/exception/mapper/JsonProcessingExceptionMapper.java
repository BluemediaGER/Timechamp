package dev.bluemedia.timechamp.api.exception.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.bluemedia.timechamp.model.response.GenericError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * {@link ExceptionMapper} implementation used to map {@link JsonProcessingException} to a response.
 *
 * @author Oliver Traber
 */
@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    /**
     * Map an given {@link Exception} to a response.
     * @param ex {@link JsonProcessingException} required to overwrite the toResponse method.
     * @return {@link Response} containing the stacktrace.
     */
    @Override
    public Response toResponse(JsonProcessingException ex) {
        GenericError error = new GenericError();
        error.error = "bad_json";
        error.message = "Your request contains invalid json. Probably you have a typo or a syntax violation.";
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

}