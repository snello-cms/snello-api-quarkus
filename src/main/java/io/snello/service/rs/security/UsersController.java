package io.snello.service.rs.security;

import io.snello.management.AppConstants;
import io.snello.service.ApiService;
import io.snello.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.snello.management.AppConstants.CONFIRM_PASSWORD;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(AppConstants.USERS_PATH)
public class UsersController {

    @Inject
    ApiService apiService;

    @Context
    UriInfo uriInfo;

    String table = AppConstants.USERS;
    String UUID = AppConstants.USERNAME;

    Logger logger = LoggerFactory.getLogger(UsersController.class);


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
        List<Map<String, Object>> list = apiService.list(table, uriInfo.getQueryParameters(), sort, l, s);
        for (Map<String, Object> uu : list) {
            uu.put(AppConstants.PASSWORD, "");
        }
        return ok(list)
                .header(AppConstants.SIZE_HEADER_PARAM, "" + apiService.count(table, uriInfo))
                .header(AppConstants.TOTAL_COUNT_HEADER_PARAM, "" + apiService.count(table, uriInfo)).build();
    }


    @GET
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response fetch(@NotNull String uuid) throws Exception {
        Map<String, Object> user = apiService.fetch(null, table, uuid, UUID);
        user.put(AppConstants.PASSWORD, "");
        return ok(user).build();
    }


    @POST
    public Response post(Map<String, Object> map) throws Exception {
        map.put(UUID, map.get(AppConstants.USERNAME));
        String pwd = PasswordUtils.createPassword((String) map.get(AppConstants.PASSWORD));
        map.put(AppConstants.PASSWORD, pwd);
        map.put(AppConstants.CREATION_DATE, Instant.now().toString());
        map.put(AppConstants.LAST_UPDATE_DATE, Instant.now().toString());
        map = apiService.create(table, map, UUID);
        map.put(AppConstants.PASSWORD, "");
        return ok(map).build();
    }

    @PUT
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response put(Map<String, Object> map, @NotNull String uuid) throws Exception {
        String pwd = (String) map.get(AppConstants.PASSWORD);
        String confirmPwd = (String) map.get(CONFIRM_PASSWORD);
        if (pwd != null && !pwd.trim().isEmpty()) {
            if (confirmPwd != null && !pwd.trim().isEmpty() &&
                    confirmPwd.equals(pwd)) {
                pwd = PasswordUtils.createPassword((String) map.get(AppConstants.PASSWORD));
                map.put(AppConstants.PASSWORD, pwd);
            } else {
                logger.info("ERROR: password != confirmPwd");
                return serverError().build();
            }
        } else {
            logger.info("no change password");
        }
        map.put(AppConstants.LAST_UPDATE_DATE, Instant.now().toString());
        map = apiService.merge(table, map, uuid, UUID);
        map.put(AppConstants.PASSWORD, "");
        return ok(map).build();
    }

    @DELETE
    @Path(AppConstants.UUID_PATH_PARAM)
    public Response delete(@NotNull String uuid) throws Exception {
        logger.info("QUI NON DOVREI SOLO PASSIVARE??");
        logger.info("QUI NON DOVREI SOLO PASSIVARE??");
        logger.info("QUI NON DOVREI SOLO PASSIVARE??");
        logger.info("QUI NON DOVREI SOLO PASSIVARE??");
        boolean result = apiService.delete(table, uuid, UUID);
        if (result) {
            return ok().build();
        }
        return serverError().build();
    }


}
