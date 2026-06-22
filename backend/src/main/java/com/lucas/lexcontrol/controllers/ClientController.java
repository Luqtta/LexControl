package com.lucas.lexcontrol.controllers;

import java.util.List;
import java.util.UUID;

import com.lucas.lexcontrol.dto.client.ClientRequest;
import com.lucas.lexcontrol.dto.client.ClientResponse;
import com.lucas.lexcontrol.services.ClientService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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

@Path("/clients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class ClientController {

    @Inject
    ClientService clientService;

    @GET
    public List<ClientResponse> list(
            @QueryParam("search") String search,
            @QueryParam("status") String status,
            @QueryParam("sort") String sort,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size
    ) {
        return clientService.list(search, status, sort, page, size);
    }

    @POST
    public Response create(@Valid ClientRequest request) {
        ClientResponse response = clientService.create(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public ClientResponse get(@PathParam("id") UUID id) {
        return clientService.get(id);
    }

    @PUT
    @Path("/{id}")
    public ClientResponse update(@PathParam("id") UUID id, @Valid ClientRequest request) {
        return clientService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        clientService.delete(id);
        return Response.noContent().build();
    }
}
