package io.snello.service.rs.system;

import io.snello.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static io.snello.management.AppConstants.DATALIST_PATH;
import static javax.ws.rs.core.Response.ok;

@Path(DATALIST_PATH)
public class DataListServiceRs {

    Logger logger = LoggerFactory.getLogger(DataListServiceRs.class);


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
