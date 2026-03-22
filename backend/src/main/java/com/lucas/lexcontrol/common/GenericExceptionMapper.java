package com.lucas.lexcontrol.common;

import java.time.Instant;
import java.util.List;

import org.jboss.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webException) {
            int status = webException.getResponse().getStatus();
            if (status >= 500) {
                LOG.error("WebApplicationException on " + (uriInfo != null ? uriInfo.getPath() : "unknown"),
                        exception);
            }
            String path = uriInfo != null ? uriInfo.getPath() : null;
            ApiError error = new ApiError(
                    Instant.now().toString(),
                    status,
                    ApiErrorCode.UNKNOWN_ERROR.getCode(),
                    Response.Status.fromStatusCode(status).getReasonPhrase(),
                    "Request failed",
                    path,
                    List.of()
            );
            return Response.status(status).entity(error).build();
        }

        LOG.error("Unhandled exception on " + (uriInfo != null ? uriInfo.getPath() : "unknown"), exception);

        String path = uriInfo != null ? uriInfo.getPath() : null;
        ApiError error = new ApiError(
                Instant.now().toString(),
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                ApiErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected error",
                path,
                List.of()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
