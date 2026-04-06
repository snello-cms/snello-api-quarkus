package io.snello.service.rs;

import io.quarkus.cache.CacheResult;
import io.snello.service.CachedDocumentReadService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static io.snello.management.AppConstants.*;

@Path(CACHE_DOCUMENTS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class CacheDocumentServiceRs {

    private static final String FETCH_CACHE = "documents-fetch";
    private static final String DOWNLOAD_WITH_NAME_CACHE = "documents-download-with-name";

    @Inject
    DocumentServiceRs documentServiceRs;

    @Inject
    CachedDocumentReadService cachedDocumentReadService;

    @Inject
    io.snello.service.ApiService apiService;

    @GET
    @Path("/{id}")
    @CacheResult(cacheName = FETCH_CACHE)
    public Response fetch(@PathParam("id") String id) throws Exception {
        return documentServiceRs.fetch(id);
    }

    @GET
    @Path(UUID_PATH_PARAM + DOWNLOAD_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("uuid") @NotNull String uuid,
                             @QueryParam(value = "format") String format) throws Exception {
        if (format != null && !format.isBlank() && hasGeneratedFormat(uuid, format)) {
            return cachedDocumentReadService.cachedDownload(uuid, format);
        }
        if (format == null || format.isBlank()) {
            return cachedDocumentReadService.cachedDownload(uuid, null);
        }
        return documentServiceRs.download(uuid, format);
    }

    @GET
    @Path(UUID_PATH_PARAM + WEBP_PATH)
    @Consumes("*/*")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response webp(@PathParam("uuid") @NotNull String uuid) throws Exception {
        if (hasGeneratedFormat(uuid, "webp")) {
            return cachedDocumentReadService.cachedWebp(uuid);
        }
        return documentServiceRs.webp(uuid);
    }

    @GET
    @Path("/{uuid}/download/{name}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Consumes(MediaType.APPLICATION_JSON)
    @CacheResult(cacheName = DOWNLOAD_WITH_NAME_CACHE)
    public Response downloadWithName(@PathParam("uuid") @NotNull String uuid,
                                     @PathParam("name") @NotNull String name) throws Exception {
        return documentServiceRs.downloadWithName(uuid, name);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response list(@Null @QueryParam(SORT_PARAM) String sort,
                         @Null @QueryParam(LIMIT_PARAM) String limit,
                         @Null @QueryParam(START_PARAM) String start,
                         @Context UriInfo uriInfo) throws Exception {
        return documentServiceRs.list(sort, limit, start, uriInfo);
    }

    private boolean hasGeneratedFormat(String uuid, String format) throws Exception {
        Map<String, Object> map = apiService.fetch(null, DOCUMENTS, uuid, UUID);
        Object formatsValue = map.get(FORMATS);
        if (!(formatsValue instanceof String formats) || formats.isBlank()) {
            return false;
        }

        String target = format.toLowerCase(Locale.ROOT);
        return Arrays.stream(formats.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(target));
    }
}
