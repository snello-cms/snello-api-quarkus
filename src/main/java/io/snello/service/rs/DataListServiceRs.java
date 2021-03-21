package io.snello.service.rs;

import io.snello.service.MetadataService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.snello.management.AppConstants.DATALIST_PATH;
import static javax.ws.rs.core.Response.ok;

@Path(DATALIST_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class DataListServiceRs {

    @Inject
    MetadataService metadataService;

    @GET
    @Path("/names")
    public Response names() throws Exception {
        return ok(metadataService.names()).build();
    }

    @GET
    @Path("/metadata/{name}")
    public Response metadata(@PathParam("name") @NotNull String name) throws Exception {
        return ok(metadataService.metadata(name)).build();
    }

    @GET
    @Path("/metadata/{name}/fielddefinitions")
    public Response fielddefinitions(@PathParam("name") @NotNull String name) throws Exception {
        return ok(metadataService.fielddefinitions(name)).build();
    }

    @GET
    @Path("/metadata/{name}/condition")
    public Response conditions(@PathParam("name") @NotNull String name) throws Exception {
        return ok(metadataService.conditions(name)).build();
    }

}
