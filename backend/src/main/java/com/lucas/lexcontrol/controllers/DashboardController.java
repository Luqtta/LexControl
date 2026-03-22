package com.lucas.lexcontrol.controllers;

import com.lucas.lexcontrol.dto.dashboard.DashboardSummary;
import com.lucas.lexcontrol.services.DashboardService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class DashboardController {

    @Inject
    DashboardService dashboardService;

    @GET
    @Path("/summary")
    public DashboardSummary summary() {
        return dashboardService.summary();
    }
}
