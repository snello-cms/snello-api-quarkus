package io.snello.service.rs;

import io.quarkus.logging.Log;
import io.snello.model.FieldDefinition;
import io.snello.model.Metadata;
import io.snello.service.ApiService;
import io.snello.util.ParamUtils;
import io.snello.util.TableKeyUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

import static io.snello.management.AppConstants.*;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.serverError;

@Path(API_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ApiServiceRs {


    @Inject
    ApiService apiService;

    @Context
    UriInfo uriInfo;

    @Inject
    SecurityContext securityContext;

    public ApiServiceRs() {
    }


    @GET
    @Path(TABLE_PATH_PARAM)
    public Response list(@NotNull @PathParam("table") String table, @QueryParam(SORT_PARAM) String sort, @QueryParam(LIMIT_PARAM) String limit, @QueryParam(START_PARAM) String start) throws Exception {
        if (sort != null) Log.info(SORT_DOT_DOT + sort);
        if (limit != null) Log.info(LIMIT_DOT_DOT + limit);
        if (start != null) Log.info(START_DOT_DOT + start);
        debug(GET.class.getName());
        int l = limit == null ? 10 : Integer.parseInt(limit);
        int s = start == null ? 0 : Integer.parseInt(start);
        Metadata metadata = apiService.metadata(table);
        if (metadata != null && metadata.api_protected) {
            MultivaluedMap<String, String> parametersMap = null;
            if (uriInfo.getQueryParameters() != null) {
                parametersMap = uriInfo.getQueryParameters();
            } else {
                parametersMap = new MultivaluedHashMap<>();
            }
            parametersMap.put(metadata.username_field, List.of(securityContext.getUserPrincipal().getName()));
        }
        long count = apiService.count(table, uriInfo);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s)).header(SIZE_HEADER_PARAM, "" + count).header(TOTAL_COUNT_HEADER_PARAM, "" + count).build();
    }


    @GET
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM)
    public Response fetch(@NotNull @PathParam("table") String table, @NotNull @PathParam("uuid") String uuid) throws Exception {
        debug(GET.class.getName());
        String key = apiService.table_key(table);
        Metadata metadata = apiService.metadata(table);
        var result = apiService.fetch(uriInfo.getQueryParameters(), table, uuid, key);
        if (metadata != null && metadata.api_protected) {
            if (!result.get(metadata.username_field).equals(securityContext.getUserPrincipal().getName())) {
                throw new Exception("Unauthorized");
            }
        }
        return ok(result).build();
    }


    @GET
    @Path(TABLE_PATH_PARAM + UUID_PATH_PARAM + EXTRA_PATH_PARAM)
    public Response get(@NotNull @PathParam("table") String table, @NotNull @PathParam("uuid") String uuid, @NotNull @PathParam("path") String path, @Null @QueryParam(SORT_PARAM) String sort, @Null @QueryParam(LIMIT_PARAM) String limit, @Null @QueryParam(START_PARAM) String start) throws Exception {
        debug(GET.class.getName());
        Metadata metadata = apiService.metadata(table);
        if (path == null) {
            throw new Exception(MSG_PATH_IS_EMPTY);
        }
        if (start == null) {
            start = _0;
        }
        if (limit == null) {
            limit = _10;
        }
        Log.info("path accessorio: " + path);
        if (path.contains("/")) {
            String[] pars = path.split(BASE_PATH);
            if (pars.length > 1) {
                return ok(apiService.fetch(uriInfo.getQueryParameters(), pars[0], pars[1], UUID)).build();
            } else {
                MultivaluedMap<String, String> parametersMap = null;
                if (uriInfo.getQueryParameters() != null) {
                    parametersMap = uriInfo.getQueryParameters();
                } else {
                    parametersMap = new MultivaluedHashMap<>();
                }
                if (metadata != null && metadata.api_protected) {
                    parametersMap.put(metadata.username_field, List.of(securityContext.getUserPrincipal().getName()));
                }
                return ok(apiService.list(pars[0], parametersMap, sort, Integer.valueOf(limit), Integer.valueOf(start))).build();
            }
        } else {
            MultivaluedMap<String, String> parametersMap = null;
            if (uriInfo.getQueryParameters() != null) {
                parametersMap = uriInfo.getQueryParameters();
            } else {
                parametersMap = null;
            }
            parametersMap.put(table + "_id", Collections.singletonList(uuid));
            parametersMap.put("join_table", List.of(table + "_" + path));
            return ok(apiService.list(path, parametersMap, sort, Integer.valueOf(limit), Integer.valueOf(start))).build();
        }
    }


    @POST
    @Path(TABLE_PATH_PARAM)
    public Response post(Map<String, Object> map, @NotNull @PathParam("table") String table) throws Exception {
        Metadata metadata = apiService.metadataWithFields(table);
        String key = metadata.table_key;
        if (metadata != null && metadata.api_protected) {
            Log.info("api protected: " + securityContext.getUserPrincipal().getName());
            Log.info("user roles admin: " + securityContext.isUserInRole("admin"));
            Log.info("user roles user: " + securityContext.isUserInRole("user"));
            Log.info("user roles manager: " + securityContext.isUserInRole("manager"));
            map.put(metadata.username_field, securityContext.getUserPrincipal().getName());
        } else {
            Log.info("api NOT protected");
        }
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
    public Response put(Map<String, Object> map, @NotNull @PathParam("table") String table, @NotNull @PathParam("uuid") String uuid) throws Exception {
        Metadata metadata = apiService.metadataWithFields(table);
        boolean renewSlug = TableKeyUtils.isSlug(metadata);
        String key = apiService.table_key(table);
        if (renewSlug) {
            String fieldSluggable = apiService.slugField(table);
            String toSlugValue = (String) map.get(fieldSluggable);
            String slugged = TableKeyUtils.createSlug(toSlugValue);
            Log.info("toSlugValue: " + toSlugValue + ", old slug: " + uuid);
            if (!uuid.equals(slugged)) {
                Log.info("renew slug");
                TableKeyUtils.generateUUid(map, metadata, apiService);
            } else {
                Log.info(" slug is the same!!");
            }
        }
        if (metadata.api_protected) {
            map.put(metadata.username_field, securityContext.getUserPrincipal().getName());
        }
        // CI VUOLE UNA TRANSAZIONE PER TENERE TUTTO INSIEME
        map = apiService.merge(table, map, uuid, key);
        //DEVO ELIMINARE TUTTI I VALORI

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
    public Response delete(@NotNull @PathParam("table") String table, @NotNull @PathParam("uuid") String uuid) throws Exception {
        debug(DELETE.class.getName());
        String key = apiService.table_key(table);
        boolean result = apiService.delete(table, uuid, key);
        if (result) return ok().build();
        return serverError().build();
    }


    private void debug(String method) {
        Log.info("------------");
        Log.info("METHOD: " + method);
        Log.info("RELATIVE PATH: " + uriInfo.getPath());
        uriInfo.getPathParameters().forEach((key, value) -> Log.info(key + ":" + value));
        Log.info("------------");
        Log.info("------------");
        uriInfo.getQueryParameters().forEach((key, value) -> Log.info("," + key + ":" + value));
        Log.info("------------");
    }

}
