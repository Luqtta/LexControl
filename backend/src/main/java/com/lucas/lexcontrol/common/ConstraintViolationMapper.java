package com.lucas.lexcontrol.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ValidationError> errors = new ArrayList<>();
        exception.getConstraintViolations().forEach(violation -> errors.add(new ValidationError(
                violation.getPropertyPath().toString(),
                violation.getMessage()
        )));

        ApiError error = new ApiError(
                Instant.now().toString(),
                Response.Status.BAD_REQUEST.getStatusCode(),
                ApiErrorCode.VALIDATION_ERROR.getCode(),
                Response.Status.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                uriInfo != null ? uriInfo.getPath() : null,
                errors
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }
}
