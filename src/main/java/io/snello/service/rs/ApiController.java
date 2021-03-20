package io.snello.service.rs;

import io.snello.model.FieldDefinition;
import io.snello.model.Metadata;
import io.snello.service.ApiService;
import io.snello.util.TableKeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(API_PATH)
public class ApiController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    ApiService apiService;

    @Context
    UriInfo uriInfo;

    public ApiController() {
    }


    @GET
    @Path(TABLE_PATH_PARAM)
    public Response list(@NotNull String table,
                         @QueryParam(SORT_PARAM) String sort,
                         @QueryParam(LIMIT_PARAM) String limit,
                         @QueryParam(START_PARAM) String start) throws Exception {
        if (sort != null)
            logger.info(SORT_DOT_DOT + sort);
        if (limit != null)
            logger.info(LIMIT_DOT_DOT + limit);
        if (start != null)
            logger.info(START_DOT_DOT + start);
        debug(GET.class.getName());
        Integer l = limit == null ? 10 : Integer.valueOf(limit);
        Integer s = start == null ? 0 : Integer.valueOf(start);
        long count = apiService.count(table, uriInfo);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s))
                .header(SIZE_HEADER_PARAM, "" + count)
                .header(TOTAL_COUNT_HEADER_PARAM, "" + count).build();
    }


    @GET
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM)
    public Response fetch(@NotNull String table, @NotNull String uuid) throws Exception {
        debug(GET.class.getName());
        String key = apiService.table_key(table);
        return ok(apiService.fetch(uriInfo.getQueryParameters(), table, uuid, key)).build();
    }


    @GET
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM + EXTRA_PATH_PARAM)
    public Response get(@NotNull String table, @NotNull String uuid, @NotNull String path,
                        @Null @QueryParam(SORT_PARAM) String sort,
                        @Null @QueryParam(LIMIT_PARAM) String limit,
                        @Null @QueryParam(START_PARAM) String start) throws Exception {
        debug(GET.class.getName());
        if (path == null) {
            throw new Exception(MSG_PATH_IS_EMPTY);
        }
        if (start == null) {
            start = _0;
        }
        if (limit == null) {
            limit = _10;
        }
        logger.info("path accessorio: " + path);
        if (path.contains("/")) {
            String[] pars = path.split(BASE_PATH);
            if (pars.length > 1) {
                return ok(apiService.fetch(uriInfo.getQueryParameters(), pars[0], pars[1], UUID)).build();
            } else {
                return ok(apiService.list(pars[0], uriInfo.getQueryParameters(), sort, Integer.valueOf(limit), Integer.valueOf(start))).build();
            }
        } else {
            MultivaluedMap<String, String> parametersMap = null;
            if (uriInfo.getQueryParameters() != null) {
                parametersMap = uriInfo.getQueryParameters();
            } else {
                parametersMap = null;
            }
            parametersMap.put(table + "_id", Arrays.asList(new String[]{uuid}));
            parametersMap.put("join_table", Arrays.asList(new String[]{table + "_" + path}));
            return ok(apiService.list(path, parametersMap, sort, Integer.valueOf(limit), Integer.valueOf(start))).build();
        }
    }


    @POST
    @Path(TABLE_PATH_PARAM)
    public Response post(Map<String, Object> map, @NotNull String table) throws Exception {
        Metadata metadata = apiService.metadataWithFields(table);
        String key = metadata.table_key;
        TableKeyUtils.generateUUid(map, metadata, apiService);
        // CI VUOLE UNA TRANSAZIONE PER TENERE TUTTO INSIEME
        for (FieldDefinition fd : metadata.fields) {
            if ("multijoin".equals(fd.type)) {
                if (map.containsKey(fd.name) && map.get(fd.name) != null) {
                    String join_table_uuids_value = (String) map.get(fd.name);
                    String[] join_table_uuids = join_table_uuids_value.split(",|;");
                    for (String ss : join_table_uuids) {
                        String join_table_name = metadata.table_name + "_" + fd.join_table_name;
                        String table_id = metadata.table_name + "_id";
                        String join_table_id = fd.join_table_name + "_id";
                        Map<String, Object> join_map = new HashMap<>();
                        join_map.put(table_id, map.get(metadata.table_key));
                        join_map.put(join_table_id, ss.trim());
                        apiService.createFromMap(join_table_name, join_map);
                    }
                    //ELIMINO I VALORI NEL CAMPO DI APPOGGIO
                    map.remove(fd.name);
                }
            }
        }
        map = apiService.create(table, map, key);
        return ok(map).build();
    }

    @PUT
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM)
    public Response put(Map<String, Object> map, @NotNull String table, @NotNull String uuid) throws Exception {
        boolean renewSlug = TableKeyUtils.isSlug(apiService.metadata(table));
        String key = apiService.table_key(table);
        if (renewSlug) {
            String fieldSluggable = apiService.slugField(table);
            String toSlugValue = (String) map.get(fieldSluggable);
            String slugged = TableKeyUtils.createSlug(toSlugValue);
            logger.info("toSlugValue: " + toSlugValue + ", old slug: " + uuid);
            if (!uuid.equals(slugged)) {
                logger.info("renew slug");
                TableKeyUtils.generateUUid(map, apiService.metadata(table), apiService);
            } else {
                logger.info(" slug is the same!!");
            }
        }
        // CI VUOLE UNA TRANSAZIONE PER TENERE TUTTO INSIEME
        map = apiService.merge(table, map, uuid, key);
        //DEVO ELIMINARE TUTTI I VALORI
        Metadata metadata = apiService.metadataWithFields(table);
        for (FieldDefinition fd : metadata.fields) {
            if ("multijoin".equals(fd.type)) {
                String join_table_name = metadata.table_name + "_" + fd.join_table_name;
                String table_id = metadata.table_name + "_id";
                apiService.delete(join_table_name, table_id, uuid);
                if (map.containsKey(fd.name) && map.get(fd.name) != null) {
                    String join_table_uuids_value = (String) map.get(fd.name);
                    String[] join_table_uuids = join_table_uuids_value.split(",|;");
                    for (String ss : join_table_uuids) {
                        String join_table_id = fd.join_table_name + "_id";
                        Map<String, Object> join_map = new HashMap<>();
                        join_map.put(table_id, map.get(metadata.table_key));
                        join_map.put(join_table_id, ss.trim());
                        apiService.createFromMap(join_table_name, join_map);
                    }
                }
                //ELIMINO I VALORI NEL CAMPO DI APPOGGIO
                map.remove(fd.name);
            }
        }
        return ok(map).build();
    }

    @DELETE
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM)
    public Response delete(@NotNull String table, @NotNull String uuid) throws Exception {
        debug(DELETE.class.getName());
        String key = apiService.table_key(table);
        boolean result = apiService.delete(table, uuid, key);
        if (result)
            return ok().build();
        return serverError().build();
    }


    private void debug(String method) {
        logger.info("------------");
        logger.info("METHOD: " + method);
        logger.info("RELATIVE PATH: " + uriInfo.getPath());
        uriInfo.getPathParameters().forEach((key, value) -> logger.info(key + ":" + value));
        logger.info("------------");
        logger.info("------------");
        uriInfo.getQueryParameters().forEach((key, value) -> logger.info("," + key + ":" + value));
        logger.info("------------");
    }

}
