package io.snello.service.rs.system;


import io.snello.management.AppConstants;
import io.snello.service.ApiService;
import io.snello.service.documents.DocumentsService;
import io.snello.util.MultipartFormUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(DOCUMENTS_PATH)
public class DocumentsController {

    Logger logger = LoggerFactory.getLogger(DocumentsController.class);

    @Inject
    ApiService apiService;

    String table = DOCUMENTS;

    @Inject
    DocumentsService documentsService;

    @Context
    UriInfo uriInfo;

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

    @GET
    @Path(UUID_PATH_PARAM)
    public Response fetch(@NotNull String uuid) throws Exception {
        return ok(apiService.fetch(null, table, uuid, UUID)).build();
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
//        return Response.ok(documentsService.streamingOutput(path, mimetype)).build();
        return null;
    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(MultipartFormDataInput multipartFormDataInput) {
        String table_name = null;
        String table_key = null;
        try {
            table_name = MultipartFormUtils.readTextField(multipartFormDataInput, TABLE_NAME);
            table_key = MultipartFormUtils.readTextField(multipartFormDataInput, TABLE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String uuid = java.util.UUID.randomUUID().toString();
//            Map<String, Object> map = documentsService.upload(
//                    MultipartFormUtils.readFileBytesInputStream(multipartFormDataInput, "file"),
//                    uuid, table_name,
//                    table_key);
//            map = apiService.create(table, map, AppConstants.UUID);
//            return ok(map).build();
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }


    @PUT
    @Path(value = UUID_PATH_PARAM)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response put(MultipartFormDataInput multipartFormDataInput,
                        @NotNull String uuid) {
        String table_name = null;
        String table_key = null;
        try {
            table_name = MultipartFormUtils.readTextField(multipartFormDataInput, TABLE_NAME);
            table_key = MultipartFormUtils.readTextField(multipartFormDataInput, TABLE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
//            Map<String, Object> map = documentsService.upload(
//                    MultipartFormUtils.readFileBytesInputStream(multipartFormDataInput, "file"),
//                    uuid, table_name, table_key);
//            map = apiService.merge(table, map, uuid, AppConstants.UUID);
//            return ok(map).build();
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

    @DELETE
    @Path(UUID_PATH_PARAM)
    public Response delete(@NotNull String uuid, @Null @QueryParam(DELETE_PARAM) String delete) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, AppConstants.UUID);
        if (delete != null && delete.toLowerCase().equals(TRUE)) {
//            documentsService.delete((String) map.get(DOCUMENT_PATH));
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
