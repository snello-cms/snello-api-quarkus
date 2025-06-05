package io.snello.service.rs;

import io.quarkus.logging.Log;
import io.smallrye.common.constraint.Nullable;
import io.snello.api.service.StorageService;
import io.snello.management.AppConstants;
import io.snello.model.events.ImageEvent;
import io.snello.model.pojo.DocumentFormData;
import io.snello.service.ApiService;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.util.Map;
import java.util.Optional;

import static io.snello.management.AppConstants.*;
import static jakarta.ws.rs.core.Response.ok;
import static jakarta.ws.rs.core.Response.serverError;

@Path(DOCUMENTS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class DocumentServiceRs {
    private static String table = DOCUMENTS;

    @Inject
    StorageService documentsService;
    
    @Inject
    ApiService apiService;

    @Inject
    Event<ImageEvent> imageEvent;

    public DocumentServiceRs() {
    }


    @GET
    @Path("/{id}")
    public Response fetch(@PathParam("id") String id) throws Exception {
        Map<String, Object> result = apiService.fetch(null, table, id, UUID);
        return ok(result).build();
    }


    @GET
    @Path(UUID_PATH_PARAM + DOWNLOAD_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("uuid") @NotNull String uuid, @QueryParam(value = "format") String format) throws Exception {
        Log.info("download - " + uuid + "," + format);
        Map<String, Object> map = apiService.fetch(null, table, uuid, AppConstants.UUID);
        String path = (String) map.get(DOCUMENT_PATH);
        String mimetype = (String) map.get(DOCUMENT_MIME_TYPE);
        String filename = (String) map.get(DOCUMENT_NAME);
        String formats = (String) map.get(FORMATS);
        if (format != null && !format.isBlank()) {
            boolean itemExists = formats != null && formats.contains(format);
            if (itemExists) {
                String duuid = uuid + "_" + format;
                String dpath = path.replace(uuid, duuid);
                StreamingOutput output = documentsService.streamingOutput(dpath, mimetype);
                return Response.ok(output)
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .build();
            } else {
                imageEvent.fireAsync(new ImageEvent(uuid, format));
            }
        }
        StreamingOutput output = documentsService.streamingOutput(path, mimetype);
        if (format != null) {
            return Response.ok(output)
                    .header("Content-Disposition", "inline; \"")
                    .build();
        } else {
            return Response.ok(output)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();
        }
    }

    @GET
    @Path("/{uuid}/download/{name}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadWithName(@PathParam("uuid") @NotNull String uuid,
                                     @PathParam("name") @NotNull String name) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, AppConstants.UUID);
        String path = (String) map.get(DOCUMENT_PATH);
        String mimetype = (String) map.get(DOCUMENT_MIME_TYPE);
        StreamingOutput output = documentsService.streamingOutput(path, mimetype);
        return Response.ok(output)
                .header("Content-Disposition", "attachment; filename=\"" + name + "\"")
                .build();
    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response persist(@MultipartForm DocumentFormData documentFormData) {
        try {
            String uuid = java.util.UUID.randomUUID().toString();
            documentFormData.uuid = uuid;
            Map<String, Object> map = documentsService.upload(documentFormData);
            map = apiService.create(table, map, AppConstants.UUID);
            return ok(map).build();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
        return serverError().build();
    }


    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@MultipartForm DocumentFormData documentFormData,
                           @PathParam("uuid") @NotNull String uuid) {
        try {
//            Map<String, Object> map = documentsService.upload(file, uuid, table_name, table_key);
            documentFormData.uuid = uuid;
            Map<String, Object> map = documentsService.upload(documentFormData);
            map = apiService.merge(table, map, uuid, AppConstants.UUID);
            return ok(map).build();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
        return serverError().build();
    }


    @Path("/{uuid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("uuid") @NotNull String uuid,
                           @Nullable @QueryParam("delete") String delete) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, AppConstants.UUID);
        if (delete != null && delete.toLowerCase().equals(TRUE)) {
            documentsService.delete((String) map.get(DOCUMENT_PATH));
        }
        if (map != null) {
            boolean result = apiService.delete(table, uuid, AppConstants.UUID);
            if (result) {
                return ok().build();
            } else {
                return serverError().build();
            }
        } else {
            return serverError().build();
        }
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response list(
            @Null @QueryParam(SORT_PARAM) String sort,
            @Null @QueryParam(LIMIT_PARAM) String limit,
            @Null @QueryParam(START_PARAM) String start,
            @Context UriInfo uriInfo) throws Exception {
        if (sort != null)
            Log.info(SORT_DOT_DOT + sort);
        else
            sort = "table_name asc";
        if (limit != null)
            Log.info(LIMIT_DOT_DOT + limit);
        if (start != null)
            Log.info(START_DOT_DOT + start);
        int l = Optional.ofNullable(limit).map(Integer::parseInt).orElse(10);
        int s = Optional.ofNullable(start).map(Integer::parseInt).orElse(0);
        return ok(apiService.list(table, uriInfo.getQueryParameters(), sort, l, s))
                .header(SIZE_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo))
                .header(TOTAL_COUNT_HEADER_PARAM, EMPTY + apiService.count(table, uriInfo)).build();
    }
}
