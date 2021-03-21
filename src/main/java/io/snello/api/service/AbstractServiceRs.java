package io.snello.api.service;

import io.snello.service.ApiService;
import io.snello.util.MetadataUtils;
import org.jboss.logging.Logger;

import javax.transaction.Transactional;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.Optional;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

public abstract class AbstractServiceRs {

    protected Logger logger = Logger.getLogger(AbstractServiceRs.class);

    protected ApiService apiService;

    private String table;
    private String defaultSort;

    public AbstractServiceRs(ApiService apiService, String table, String defaultSort) {
        this.apiService = apiService;
        this.table = table;
        this.defaultSort = defaultSort;
    }

    public AbstractServiceRs() {
    }

    @POST
    public Response persist(Map<String, Object> map) throws Exception {
        if (map.get(TABLE_NAME) == null) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(map.get(TABLE_NAME))) {
            throw new Exception(MSG_TABLE_NAME_IS_RESERVED);
        }
        map.put(UUID, java.util.UUID.randomUUID().toString());
        map = apiService.create(table, map, UUID);
        postCreate(map);
        return ok(map).build();
    }


    @GET
    @Path("/{id}")
    public Response fetch(@PathParam("id") String id) throws Exception {
        Map<String, Object> result = apiService.fetch(null, table, id, UUID);
        postFetch(result);
        return ok(result).build();
    }


    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id,
                           Map<String, Object> map) throws Exception {
        if (map.get(TABLE_NAME) == null) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(map.get(TABLE_NAME))) {
            throw new Exception(MSG_TABLE_NAME_IS_EMPTY);
        }
        map = apiService.merge(table, map, id, UUID);
        postUpdate(map);
        return ok(map).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) throws Exception {
        boolean result = apiService.delete(table, id, UUID);
        boolean deleteFieldDefinitionsByMetadataUuid = apiService.deleteFieldDefinitionsByMetadataUuid(id);
        if (result && deleteFieldDefinitionsByMetadataUuid) {
            postDelete(id);
            return ok().build();
        }
        return serverError().build();
    }

    @GET
    public Response list(
            @Null @QueryParam(SORT_PARAM) String sort,
            @Null @QueryParam(LIMIT_PARAM) String limit,
            @Null @QueryParam(START_PARAM) String start,
            @Context UriInfo uriInfo) throws Exception {
        if (sort != null)
            logger.info(SORT_DOT_DOT + sort);
        else
            sort = defaultSort;
        if (limit != null)
            logger.info(LIMIT_DOT_DOT + limit);
        if (start != null)
            logger.info(START_DOT_DOT + start);
        int l = Optional.ofNullable(limit).map(Integer::parseInt).orElse(10);
        int s = Optional.ofNullable(start).map(Integer::parseInt).orElse(0);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s))
                .header(SIZE_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo))
                .header(TOTAL_COUNT_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo)).build();
    }

    protected void postUpdate(Map<String, Object> object) throws Exception
    {
    }

    protected void postDelete(String id) throws Exception
    {
    }

    protected void postFetch(Map<String, Object> object) throws Exception
    {
    }

    protected void postCreate(Map<String, Object> object) throws Exception
    {
    }

    protected ApiService getApiService(){
        return apiService;
    }
}