package dev.bluemedia.timechamp.api.exception.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ExceptionMapper} implementation used to map {@link ConstraintViolationException} to a response.
 *
 * @author Oliver Traber
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    /**
     * Map a given {@link ConstraintViolationException} to a response json.
     * @param ex {@link ConstraintViolationException} that should be mapped.
     * @return {@link Response} containing the mapped {@link ConstraintViolationException}.
     */
    @Override
    public Response toResponse(ConstraintViolationException ex) {

        List<ConstrainError> errors = new ArrayList<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            String[] propertyPath = cv.getPropertyPath().toString().split("\\.");
            errors.add(new ConstrainError(propertyPath[propertyPath.length - 1], cv.getMessage()));
        }

        ObjectMapper mapper = new ObjectMapper();
        String errorList = "";
        try {
            errorList = mapper.writeValueAsString(errors);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        errorList = "{\"error\": " + errorList + "}";
        return Response.status(Response.Status.BAD_REQUEST).entity(errorList).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Model for a single constrain error.
     */
    private static class ConstrainError {

        /** Name of the json field that generated the error */
        @JsonProperty("name")
        private String name;

        /** Reason why the error was generated */
        @JsonProperty("code")
        private String code;

        /** Default constructor for this model */
        public ConstrainError(String name, String code) {
            this.name = name;
            this.code = code;
        }
    }

}