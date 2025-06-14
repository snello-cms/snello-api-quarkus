package io.snello.service.rs;

import io.quarkus.logging.Log;
import io.snello.service.ApiService;
import io.snello.service.TemplateService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import static io.snello.management.AppConstants.*;
import static jakarta.ws.rs.core.Response.ok;

@Path(PAGES_PATH)
@ApplicationScoped
public class TemplateServiceRs {


    @Inject
    ApiService apiService;

    @Context
    UriInfo uriInfo;

    @Inject
    TemplateService templateService;

    public TemplateServiceRs() {
    }


    @GET
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM)
    @Produces(MediaType.TEXT_HTML)
    public Response fetch(@NotNull @PathParam("table") String table,
                          @NotNull @PathParam("uuid") String uuid) throws Exception {
        debug(GET.class.getName());
        String key = apiService.table_key(table);
        var map = apiService.fetch(uriInfo.getQueryParameters(), table, uuid, key);
        templateService.parse(map, uriInfo.getQueryParameters());
        return ok().build();
    }


    private void debug(String method) {
        Log.info("------------");
        Log.info("METHOD: " + method);
        Log.info("RELATIVE PATH: " + uriInfo.getPath());
        uriInfo.getPathParameters().forEach((key, value) -> Log.info(key + ":" + value));
        Log.info("------------");
        Log.info("------------");
        uriInfo.getQueryParameters().forEach((key, value) -> Log.info("," + key + ":" + value));
        Log.info("------------");
    }

}
