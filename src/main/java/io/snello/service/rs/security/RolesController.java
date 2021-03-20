package io.snello.service.rs.security;

import io.snello.management.AppConstants;
import io.snello.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;


@Path(AppConstants.ROLES_PATH)
public class RolesController {

    @Inject
    ApiService apiService;

    @Context
    UriInfo uriInfo;

    String table = AppConstants.ROLES;
    String UUID = AppConstants.NAME;

    Logger logger = LoggerFactory.getLogger(RolesController.class);


    @GET
    public Response list(@Null @QueryParam(AppConstants.SORT_PARAM) String sort,
                         @Null @QueryParam(AppConstants.LIMIT_PARAM) String limit,
                         @Null @QueryParam(AppConstants.START_PARAM) String start) throws Exception {
        if (sort != null)
            logger.info(AppConstants.SORT_DOT_DOT + sort);
        if (limit != null)
            logger.info(AppConstants.LIMIT_DOT_DOT + limit);
        if (start != null)
            logger.info(AppConstants.START_DOT_DOT + start);
        Integer l = limit == null ? 10 : Integer.valueOf(limit);
        Integer s = start == null ? 0 : Integer.valueOf(start);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s))
                .header(AppConstants.SIZE_HEADER_PARAM, AppConstants.EMPTY + apiService.count(table, uriInfo))
                .header(AppConstants.TOTAL_COUNT_HEADER_PARAM, AppConstants.EMPTY + apiService.count(table, uriInfo)).build();
    }


    @GET
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response fetch(@NotNull String uuid) throws Exception {
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }


    @POST
    public Response post(Map<String, Object> map) throws Exception {
        map.put(UUID, map.get(AppConstants.NAME));
        map = apiService.create(table, map, UUID);
        return ok(map).build();
    }

    @PUT
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response put(Map<String, Object> map,@NotNull String uuid) throws Exception {
        map = apiService.merge(table, map, uuid, UUID);
        return ok(map).build();
    }

    @DELETE
            @Path(AppConstants.UUID_PATH_PARAM)
    public Response delete(@NotNull String uuid) throws Exception {
        boolean result = apiService.delete(table, uuid, UUID);
        if (result) {
            return ok().build();
        }
        return serverError().build();
    }
}
