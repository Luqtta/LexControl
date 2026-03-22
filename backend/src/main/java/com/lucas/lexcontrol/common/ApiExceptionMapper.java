package com.lucas.lexcontrol.common;

import java.time.Instant;
import java.util.List;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ApiException exception) {
        ApiError error = new ApiError(
                Instant.now().toString(),
                exception.getStatus(),
                exception.getErrorCode().getCode(),
                Response.Status.fromStatusCode(exception.getStatus()).getReasonPhrase(),
                exception.getMessage(),
                uriInfo != null ? uriInfo.getPath() : null,
                List.of()
        );
        return Response.status(exception.getStatus()).entity(error).build();
    }
}
