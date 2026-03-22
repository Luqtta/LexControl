package com.lucas.lexcontrol.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.lucas.lexcontrol.dto.transaction.TransactionRequest;
import com.lucas.lexcontrol.dto.transaction.TransactionResponse;
import com.lucas.lexcontrol.entities.TransactionType;
import com.lucas.lexcontrol.services.TransactionService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class TransactionController {

    @Inject
    TransactionService transactionService;

    @GET
    public List<TransactionResponse> list(
            @QueryParam("type") TransactionType type,
            @QueryParam("clientId") UUID clientId,
            @QueryParam("from") LocalDate from,
            @QueryParam("to") LocalDate to,
            @QueryParam("sort") String sort
    ) {
        return transactionService.list(type, clientId, from, to, sort);
    }

    @POST
    public Response create(@Valid TransactionRequest request) {
        TransactionResponse response = transactionService.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public TransactionResponse update(@PathParam("id") UUID id, @Valid TransactionRequest request) {
        return transactionService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        transactionService.delete(id);
        return Response.noContent().build();
    }
}
