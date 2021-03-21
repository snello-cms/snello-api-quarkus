package io.snello.service.rs.system;

import io.snello.api.service.AbstractServiceRs;
import io.snello.model.Metadata;
import io.snello.model.events.MetadataCreateUpdateEvent;
import io.snello.model.events.MetadataDeleteEvent;
import io.snello.service.ApiService;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;

@Path(METADATA_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetadataServiceRs extends AbstractServiceRs {
    private static String table = METADATAS;

    @Inject
    Event eventPublisher;


    @Inject
    MetadataServiceRs(ApiService apiService) {
        super(apiService, METADATAS, "table_name asc");
    }

    MetadataServiceRs() {
    }

    @Override
    protected void postUpdate(Map<String, Object> map) {
        eventPublisher.fireAsync(new MetadataCreateUpdateEvent(map));
    }

    @Override
    protected void postDelete(String id) {
        eventPublisher.fireAsync(new MetadataDeleteEvent(id));
    }

    @Override
    protected void postCreate(Map<String, Object> map) {
        eventPublisher.fireAsync(new MetadataCreateUpdateEvent(map));
    }

    @GET
    @Path(UUID_PATH_PARAM_CREATE)
    public Response createTable(@NotNull String uuid) throws Exception {
        Metadata metadata = getApiService().createMetadataTable(uuid);
        Map<String, Object> updateMetadataMap = new HashMap<>();
        updateMetadataMap.put(CREATED, true);
        getApiService().merge(METADATAS, updateMetadataMap, uuid, UUID);
        return ok(getApiService().fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path(UUID_PATH_PARAM_DELETE)
    public Response deleteTable(@NotNull String uuid) throws Exception {
        boolean result = getApiService().deleteTable(uuid);
        if (result) {
            Map<String, Object> updateMetadataMap = new HashMap<>();
            updateMetadataMap.put(CREATED, false);
            getApiService().merge(METADATAS, updateMetadataMap, uuid, UUID);
        }
        return ok(getApiService().fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path(UUID_PATH_PARAM_TRUNCATE)
    public Response truncateTable(@NotNull String uuid) throws Exception {
        getApiService().truncateTable(uuid);
        return ok(getApiService().fetch(null, table, uuid, UUID)).build();
    }


}
