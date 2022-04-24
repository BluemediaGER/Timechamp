package dev.bluemedia.timechamp.api.exception.mapper;

import dev.bluemedia.timechamp.model.response.GenericError;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@Provider
public class SqlExceptionMapper implements ExceptionMapper<SQLException> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(SqlExceptionMapper.class.getName());

    @Override
    public Response toResponse(SQLException ex) {
        GenericError error = new GenericError();
        error.error = "database_error";
        error.message = "Querying the database failed";
        LOG.error("An unexpected error occurred", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON).entity(error).build();
    }

}
