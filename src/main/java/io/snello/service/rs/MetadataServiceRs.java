package io.snello.service.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.snello.api.service.AbstractServiceRs;
import io.snello.model.FieldDefinition;
import io.snello.model.Metadata;
import io.snello.model.events.MetadataCreateUpdateEvent;
import io.snello.model.events.MetadataDeleteEvent;
import io.snello.model.pojo.JsonFormData;
import io.snello.service.ApiService;
import io.snello.util.MetadataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @POST
    @Path("/export")
    public Response export(Map<String, Object> body) throws Exception {
        @SuppressWarnings("unchecked")
        List<String> uuids = (List<String>) body.get("metadatas");
        if (uuids == null || uuids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("metadatas list is required").build();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String uuid : uuids) {
            Map<String, Object> raw = getApiService().fetch(null, METADATAS, uuid, UUID);
            if (raw != null) {
                String tableName = (String) raw.get(TABLE_NAME);
                Metadata full = getApiService().metadataWithFields(tableName);
                if (full != null) {
                    result.add(Map.of("metadata", full, "fields", full.fields != null ? full.fields : List.of()));
                }
            }
        }
        String filename = "metadatas-export.json";
        return Response.ok(Map.of("metadatas", result))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importMetadatas(@MultipartForm JsonFormData formData) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(formData.data, Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metadatasList = (List<Map<String, Object>>) body.get("metadatas");
        if (metadatasList == null || metadatasList.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("metadatas list is empty or missing").build();
        }
        // Fase 1: verifica che nessun metadata esista già
        for (Map<String, Object> entry : metadatasList) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metaMap = (Map<String, Object>) entry.get("metadata");
            if (metaMap == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("invalid entry: missing 'metadata' object").build();
            }
            String tableName = (String) metaMap.get(TABLE_NAME);
            if (tableName == null || tableName.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("metadata missing table_name").build();
            }
            if (getApiService().metadata(tableName) != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("metadata already exists: " + tableName).build();
            }
        }
        // Fase 2: salva i metadati e le field definitions
        List<Map<String, Object>> saved = new ArrayList<>();
        for (Map<String, Object> entry : metadatasList) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metaMap = (Map<String, Object>) entry.get("metadata");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fieldsList = (List<Map<String, Object>>) entry.get("fields");
            // Salva il metadata
            Map<String, Object> savedMeta = getApiService().createIfNotExists(METADATAS, metaMap, UUID);
            eventCreateUpdatePublisher.fireAsync(new MetadataCreateUpdateEvent(savedMeta));
            // Salva le field definitions
            if (fieldsList != null) {
                for (Map<String, Object> fdMap : fieldsList) {
                    getApiService().createFromMap(FIELD_DEFINITIONS, fdMap);
                }
            }
            saved.add(savedMeta);
        }
        return ok(Map.of("imported", saved)).build();
    }

}
