package io.snello.service.rs.system;

import io.snello.model.Metadata;
import io.snello.model.events.MetadataCreateUpdateEvent;
import io.snello.model.events.MetadataDeleteEvent;
import io.snello.service.ApiService;
import io.snello.util.MetadataUtils;
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
import java.util.HashMap;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(METADATA_PATH)
public class MetadataController {


    @Inject
    ApiService apiService;

    static String table = METADATAS;
    static String default_sort = " table_name asc ";


    @Inject
    Event eventPublisher;

    @Context
    UriInfo uriInfo;


    Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @GET
    public Response list(
            @Null @QueryParam(SORT_PARAM) String sort,
            @Null @QueryParam(LIMIT_PARAM) String limit,
            @Null @QueryParam(START_PARAM) String start) throws Exception {
        if (sort != null)
            logger.info(SORT_DOT_DOT + sort);
        else
            sort = default_sort;
        if (limit != null)
            logger.info(LIMIT_DOT_DOT + limit);
        if (start != null)
            logger.info(START_DOT_DOT + start);
        Integer l = limit == null ? 10 : Integer.valueOf(limit);
        Integer s = start == null ? 0 : Integer.valueOf(start);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s))
                .header(SIZE_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo))
                .header(TOTAL_COUNT_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo)).build();
    }


    @GET
    @Path(UUID_PATH_PARAM)
    public Response fetch(@NotNull String uuid) throws Exception {
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path(UUID_PATH_PARAM_CREATE)
    public Response createTable(@NotNull String uuid) throws Exception {
        Metadata metadata = apiService.createMetadataTable(uuid);
        Map<String, Object> updateMetadataMap = new HashMap<>();
        updateMetadataMap.put(CREATED, true);
        apiService.merge(METADATAS, updateMetadataMap, uuid, UUID);
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path(UUID_PATH_PARAM_DELETE)
    public Response deleteTable(@NotNull String uuid) throws Exception {
        boolean result = apiService.deleteTable(uuid);
        if (result) {
            Map<String, Object> updateMetadataMap = new HashMap<>();
            updateMetadataMap.put(CREATED, false);
            apiService.merge(METADATAS, updateMetadataMap, uuid, UUID);
        }
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path(UUID_PATH_PARAM_TRUNCATE)
    public Response truncateTable(@NotNull String uuid) throws Exception {
        apiService.truncateTable(uuid);
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
    }


    @POST
    public Response post(Map<String, Object> map) throws Exception {
        if (map.get(TABLE_NAME) == null) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(map.get(TABLE_NAME))) {
            throw new Exception(MSG_TABLE_NAME_IS_RESERVED);
        }
        map.put(UUID, java.util.UUID.randomUUID().toString());
        map = apiService.create(table, map, UUID);
        eventPublisher.fireAsync(new MetadataCreateUpdateEvent(map));
        return ok(map).build();
    }

    @PUT
    @Path(UUID_PATH_PARAM)
    public Response put(Map<String, Object> map, @NotNull String uuid) throws Exception {
        if (map.get(TABLE_NAME) == null) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(map.get(TABLE_NAME))) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        map = apiService.merge(table, map, uuid, UUID);
        eventPublisher.fireAsync(new MetadataCreateUpdateEvent(map));
        return ok(map).build();
    }

    @DELETE
    @Path(UUID_PATH_PARAM)
    public Response delete(@NotNull String uuid) throws Exception {
        boolean result = apiService.delete(table, uuid, UUID);
        boolean deleteFieldDefinitionsByMetadataUuid = apiService.deleteFieldDefinitionsByMetadataUuid(uuid);
        if (result && deleteFieldDefinitionsByMetadataUuid) {
            eventPublisher.fireAsync(new MetadataDeleteEvent(uuid));
            return ok().build();
        }
        return serverError().build();
    }
}
