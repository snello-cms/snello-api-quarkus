package io.snello.service.rs.system;





import io.snello.model.FieldDefinition;
import io.snello.model.Link;
import io.snello.model.Metadata;
import io.snello.model.events.MetadataCreateUpdateEvent;
import io.snello.service.ApiService;
import io.snello.service.MetadataService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(LINKS_PATH)
public class LinksController {

    @Inject
    ApiService apiService;

    @Inject
    MetadataService metadataService;

    @Inject
    Event eventPublisher;

    @Context
    UriInfo uriInfo;

    static String table = LINKS;


    Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @GET
    public Response list(
                                @Null @QueryParam(SORT_PARAM) String sort,
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
                .header(SIZE_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo))
                .header(TOTAL_COUNT_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo)).build();
    }


    @GET @Path(UUID_PATH_PARAM)
    public Response fetch( @NotNull String uuid) throws Exception {
        return ok(apiService.fetch(null, table, uuid, NAME)).build();
    }


    @POST
    public Response post(Map<String, Object> map) throws Exception {
        if (map.get(NAME) == null) {
            throw new Exception(MSG_NAME_PARAM_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(map.get(NAME))) {
            throw new Exception(MSG_NAME_PARAM_IS_RESERVED);
        }
        map.put(NAME, map.get(NAME));
        map = apiService.create(table, map, NAME);
        return ok(map).build();
    }

    @PUT
    @Path(UUID_PATH_PARAM)
    public Response put(Map<String, Object> map, @NotNull String uuid) throws Exception {
        if (map.get(NAME) == null) {
            throw new Exception(MSG_NAME_PARAM_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(map.get(NAME))) {
            throw new Exception(MSG_NAME_PARAM_IS_RESERVED);
        }
        map = apiService.merge(table, map, uuid, NAME);
        return ok(map).build();
    }

    @DELETE @Path(UUID_PATH_PARAM)
    public Response delete( @NotNull String uuid) throws Exception {
        boolean result = apiService.delete(table, uuid, NAME);
        if (result) {
            return ok().build();
        }
        return serverError().build();
    }

    @GET @Path(UUID_PATH_PARAM_CREATE)
    public Response create(@NotNull String uuid) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, NAME);
        map.put(CREATED, true);
        apiService.merge(table, map, uuid, NAME);
        Link link = new Link(map);
        Metadata metadataOriginal = metadataService.metadata(link.metadata_name);
        Metadata metadata = new Metadata();
        metadata.table_name = link.name;
        metadata.uuid = java.util.UUID.randomUUID().toString();
        metadata.table_key = UUID;
        metadata.table_key_type = UUID;
        metadata.created = true;
        apiService.create(METADATAS, metadata.toMap(), UUID);

        String[] fields = link.labels.split(COMMA_MIXED);
        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        for (String name : fields) {
            FieldDefinition fieldDefinition = new FieldDefinition();
            fieldDefinition.uuid = java.util.UUID.randomUUID().toString();
            fieldDefinition.label = name;
            fieldDefinition.searchable = true;
            fieldDefinition.search_condition = "contains";
            fieldDefinition.search_field_name = name + "_contains";
            fieldDefinition.show_in_list = true;
            fieldDefinition.name = name;
            fieldDefinition.type = JOIN;
            fieldDefinition.metadata_name = metadata.table_name;
            fieldDefinition.metadata_uuid = metadata.uuid;
            fieldDefinition.join_table_key = metadataOriginal.table_key;
            fieldDefinition.join_table_name = metadataOriginal.table_name;
            fieldDefinition.join_table_select_fields = link.metadata_searchable_field;
            fieldDefinitions.add(fieldDefinition);
            apiService.create(FIELD_DEFINITIONS, fieldDefinition.toMap(), UUID);
        }
        metadataService.createTableFromMetadataAndFields(metadata, fieldDefinitions);
        MetadataCreateUpdateEvent metadataCreateUpdateEvent = new MetadataCreateUpdateEvent(metadata);
        eventPublisher.fireAsync(metadataCreateUpdateEvent);
        return ok().build();
    }
}
