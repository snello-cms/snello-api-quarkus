package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.model.Metadata;
import io.snello.model.events.MetadataCreateUpdateEvent;
import io.snello.model.events.MetadataDeleteEvent;
import io.snello.service.ApiService;
import io.snello.util.MetadataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.serverError;

@Path(METADATA_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MetadataServiceRs extends AbstractServiceRs {
    private static String table = METADATAS;

    @Inject
    Event<MetadataCreateUpdateEvent> eventCreateUpdatePublisher;
    @Inject
    Event<MetadataDeleteEvent> eventDeletePublisher;


    @Inject
    MetadataServiceRs(ApiService apiService) {
        super(apiService, METADATAS, "table_name asc");
    }

    public MetadataServiceRs() {
    }

    @Override
    protected void postUpdate(Map<String, Object> map) {
        eventCreateUpdatePublisher.fireAsync(new MetadataCreateUpdateEvent(map));
    }

    @Override
    protected void postDelete(String id) {
        eventDeletePublisher.fireAsync(new MetadataDeleteEvent(id));
    }

    @Override
    protected void postPersist(Map<String, Object> map) {
        eventCreateUpdatePublisher.fireAsync(new MetadataCreateUpdateEvent(map));
    }

    @GET
    @Path("/{uuid}/create")
    public Response createTable(@PathParam("uuid") @NotNull String uuid) throws Exception {
        Metadata metadata = getApiService().createMetadataTable(uuid);
        Map<String, Object> updateMetadataMap = new HashMap<>();
        updateMetadataMap.put(CREATED, true);
        getApiService().merge(METADATAS, updateMetadataMap, uuid, UUID);
        return ok(getApiService().fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path("/{uuid}/delete")
    public Response deleteTable(@PathParam("uuid") @NotNull String uuid) throws Exception {
        boolean result = getApiService().deleteTable(uuid);
        if (result) {
            Map<String, Object> updateMetadataMap = new HashMap<>();
            updateMetadataMap.put(CREATED, false);
            getApiService().merge(METADATAS, updateMetadataMap, uuid, UUID);
        }
        return ok(getApiService().fetch(null, table, uuid, UUID)).build();
    }

    @GET
    @Path("/{uuid}/truncate")
    public Response truncateTable(@PathParam("uuid") @NotNull String uuid) throws Exception {
        getApiService().truncateTable(uuid);
        return ok(getApiService().fetch(null, table, uuid, UUID)).build();
    }

    @Override
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) throws Exception {
        preDelete(id);
        boolean result = getApiService().delete(table, id, UUID);
        boolean deleteFieldDefinitionsByMetadataUuid = getApiService().deleteFieldDefinitionsByMetadataUuid(id);
        if (result && deleteFieldDefinitionsByMetadataUuid) {
            postDelete(id);
            return ok().build();
        }
        return serverError().build();
    }

    @Override
    protected void preUpdate(Map<String, Object> object) throws Exception {
        if (object.get(TABLE_NAME) == null) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(object.get(TABLE_NAME))) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
    }

    @Override
    protected void prePersist(Map<String, Object> object) throws Exception {
        if (object.get(TABLE_NAME) == null) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(object.get(TABLE_NAME))) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
    }


}
