package io.snello.service.rs.system;

import io.snello.model.events.ConditionCreateUpdateEvent;
import io.snello.model.events.ConditionDeleteEvent;
import io.snello.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(CONDITIONS_PATH)
public class ConditionsController {


    @Inject
    ApiService apiService;

    String table = CONDITIONS;

    @Inject
    Event eventPublisher;

    @Context
    UriInfo uriInfo;


    Logger logger = LoggerFactory.getLogger(ConditionsController.class);


    @GET
    public Response list(@Null @QueryParam(SORT_PARAM) String sort,
                         @Null @QueryParam(LIMIT_PARAM) String limit,
                         @Null @QueryParam(START_PARAM) String start) throws Exception {
        if (sort != null)
            logger.info(SORT_DOT_DOT + sort);
        if (limit != null)
            logger.info(LIMIT_DOT_DOT + limit);
        if (start != null)
            logger.info(START_DOT_DOT + start);
        Integer l = limit == null ? 10 : Integer.valueOf(limit);
        Integer s = start == null ? 0 : Integer.valueOf(start);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s))
                .header(SIZE_HEADER_PARAM, "" + apiService.count(table, uriInfo))
                .header(TOTAL_COUNT_HEADER_PARAM, "" + apiService.count(table, uriInfo)).build();
    }


    @GET @Path(UUID_PATH_PARAM)
    public Response fetch( @NotNull String uuid) throws Exception {
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }


    @POST
    public Response post(Map<String, Object> map) throws Exception {
        map.put(UUID, java.util.UUID.randomUUID().toString());
        map = apiService.create(table, map, UUID);
        eventPublisher.fireAsync(new ConditionCreateUpdateEvent(map));
        return ok(map).build();
    }

    @PUT
    @Path(UUID_PATH_PARAM)
    public Response put(Map<String, Object> map, @NotNull String uuid) throws Exception {
        map = apiService.merge(table, map, uuid, UUID);
        eventPublisher.fireAsync(new ConditionCreateUpdateEvent(map));
        return ok(map).build();
    }

    @DELETE @Path(UUID_PATH_PARAM)
    public Response delete( @NotNull String uuid) throws Exception {
        boolean result = apiService.delete(table, uuid, UUID);
        if (result) {
            eventPublisher.fireAsync(new ConditionDeleteEvent(uuid));
            return ok().build();
        }
        return serverError().build();
    }
}