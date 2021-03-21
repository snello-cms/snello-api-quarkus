package io.snello.service.rs.system;

import io.snello.api.service.AbstractServiceRs;
import io.snello.api.service.StorageService;
import io.snello.management.AppConstants;
import io.snello.model.pojo.DocumentFormData;
import io.snello.service.ApiService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(DOCUMENTS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentServiceRs extends AbstractServiceRs {
    private static String table = DOCUMENTS;

    @Inject
    StorageService documentsService;

    @Inject
    DocumentServiceRs(ApiService apiService) {
        super(apiService, DOCUMENTS, "table_name asc");
    }


    @GET
    @Path(UUID_PATH_PARAM + DOWNLOAD_PATH)
    public Response download(@NotNull String uuid) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, AppConstants.UUID);
        String path = (String) map.get(DOCUMENT_PATH);
        String mimetype = (String) map.get(DOCUMENT_MIME_TYPE);
//        return documentsService.streamingOutput(path, mimetype);
        return null;
    }

    @GET
    @Path(UUID_PATH_PARAM + DOWNLOAD_PATH + "/{name}")
    public Response downloadWithName(@NotNull String uuid, @NotNull String name) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, AppConstants.UUID);
        String path = (String) map.get(DOCUMENT_PATH);
        String mimetype = (String) map.get(DOCUMENT_MIME_TYPE);
//        return documentsService.streamingOutput(path, mimetype);
        return null;

    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(DocumentFormData documentFormData) {
        try {
            String uuid = java.util.UUID.randomUUID().toString();
            Map<String, Object> map = documentsService.upload(documentFormData);
            map = apiService.create(table, map, AppConstants.UUID);
            return ok(map).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }


    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path(UUID_PATH_PARAM)
    public Response put(DocumentFormData documentFormData,
                        @NotNull String uuid) {
        try {
//            Map<String, Object> map = documentsService.upload(file, uuid, table_name, table_key);
            Map<String, Object> map = documentsService.upload(documentFormData);
            map = apiService.merge(table, map, uuid, AppConstants.UUID);
            return ok(map).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }


    @Path(UUID_PATH_PARAM)
    @DELETE
    public Response delete(@NotNull String uuid, @Nullable @QueryParam(DELETE_PARAM) String delete) throws Exception {
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
}
