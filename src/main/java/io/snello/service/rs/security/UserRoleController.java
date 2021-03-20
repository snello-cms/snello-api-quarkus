package io.snello.service.rs.security;

import io.snello.management.AppConstants;
import io.snello.service.ApiService;
import io.snello.service.rs.system.MetadataController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;


@Path(AppConstants.USER_ROLES_PATH)
public class UserRoleController {

    @Inject
    ApiService apiService;

    @Context
    UriInfo uriInfo;

    static String table = AppConstants.USER_ROLES;
    String UUID = AppConstants.USERNAME;


    Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @GET
    public Response list(
            @Null @QueryParam(AppConstants.SORT_PARAM) String sort,
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
                .header(AppConstants.SIZE_HEADER_PARAM, "" + apiService.count(table, uriInfo))
                .header(AppConstants.TOTAL_COUNT_HEADER_PARAM, "" + apiService.count(table, uriInfo)).build();
    }


    @GET
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response fetch(@NotNull String uuid) throws Exception {
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }


    @POST
    public Response post(Map<String, Object> map) throws Exception {
        if (map.get(AppConstants.USERNAME) == null) {
            throw new Exception(AppConstants.MSG_USERNAME_IS_EMPTY);
        }
        String roleString = (String) map.get(AppConstants.ROLE);
        String[] roles = roleString.split(AppConstants.COMMA);
        for (String role : roles) {
            List<Object> userRole = new ArrayList<>();
            userRole.add(map.get(AppConstants.USERNAME));
            userRole.add(role);
            apiService.createUserRole(userRole);
        }

        return ok(map).build();
    }

    @PUT
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response put(@NotNull String uuid) throws Exception {
        // )(AppConstants.MSG_NOT_IMPLEMENTED)
        return Response.status(Response.Status.BAD_REQUEST).build();
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
