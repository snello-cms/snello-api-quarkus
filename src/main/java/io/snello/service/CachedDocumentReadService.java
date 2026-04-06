package io.snello.service;

import io.quarkus.cache.CacheResult;
import io.snello.service.rs.DocumentServiceRs;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;

@Singleton
public class CachedDocumentReadService {

    private static final String DOWNLOAD_CACHE = "documents-download";
    private static final String WEBP_CACHE = "documents-webp";

    @Inject
    DocumentServiceRs documentServiceRs;

    @CacheResult(cacheName = DOWNLOAD_CACHE)
    public Response cachedDownload(@NotNull String uuid, String format) throws Exception {
        return documentServiceRs.download(uuid, format);
    }

    @CacheResult(cacheName = WEBP_CACHE)
    public Response cachedWebp(@NotNull String uuid) throws Exception {
        return documentServiceRs.webp(uuid);
    }
}