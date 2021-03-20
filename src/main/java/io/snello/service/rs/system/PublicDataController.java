package io.snello.service.rs.system;


import io.snello.model.ResourceFile;
import io.snello.util.MultipartFormUtils;
import io.snello.util.ResourceFileUtils;
import io.snello.util.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(PUBLIC_DATA_PATH)
public class PublicDataController {

    @Context
    UriInfo uriInfo;

    @ConfigProperty(name = SYSTEM_DOCUMENTS_BASE_PATH)
    List<String> basePaths;

    Logger logger = LoggerFactory.getLogger(getClass());


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(MultipartFormDataInput multipartFormDataInput) {
        File file = null;
        try {
            file = MultipartFormUtils.readFileBytes(multipartFormDataInput, "file");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (file == null || !file.getName().endsWith(ZIP)) {
            return null;
        }
        try {
            String basePath = basePaths.get(0);
            //devo usare la cartella sopra quella dei files
            java.nio.file.Path path = java.nio.file.Path.of(basePath.replace(FILE_DOT_DOT, EMPTY)
                    .replace("\"", EMPTY)
                    .replace(FILES, EMPTY)
            );
            Map<String, Object> map = null;
            File[] allContents = path.toFile().listFiles();
            if (allContents != null) {
                for (File contentFile : allContents) {
                    logger.info("file: " + contentFile.getName());
                    if (contentFile.getName().equals(FILES)) {
                        continue;
                    }
                    logger.info("to be deleted: " + contentFile.getName());
                    if (contentFile.isDirectory()) {
                        deleteDirectory(contentFile);
                    } else {
                        contentFile.delete();
                    }
                }
            }
            File tempFile = File.createTempFile(java.util.UUID.randomUUID().toString(), DOT_ZIP);
            Files.write(tempFile.toPath(), FileUtils.readFileToByteArray(file));
            ZipUtils.unzip(tempFile.getAbsolutePath(), path.toFile().getAbsolutePath());
            tempFile.delete();
            return ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @GET
    @Path("/folders/{folderEncoded}")
    public Response resourceFileList(@Null String folderEncoded) {
        String folderName = null;
        if (folderEncoded != null && !folderEncoded.trim().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(folderEncoded);
            folderName = new String(decodedBytes);
        } else {
            folderName = basePaths.get(0);
        }
        try {
            File[] allContents = new File(folderName).listFiles();
            return ok(ResourceFileUtils.fromFiles(allContents)).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

    @POST
    @Path("/folders/{folderEncoded}")
    public Response createFolder(@Null String folderEncoded, Map<String, Object> map) throws Exception {
        String folderName = null;
        if (folderEncoded != null && !folderEncoded.trim().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(folderEncoded);
            folderName = new String(decodedBytes);
        } else {
            folderName = basePaths.get(0);
        }
        try {
            ResourceFile resourceFile = new ResourceFile(map, folderName);
            File file = new File(resourceFile.path);
            boolean rs = file.mkdir();
            return ok(rs).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

    @DELETE
    @Path("/folders/{folderEncoded}")
    public Response deleteFolderOrFile(@NotNull String folderEncoded) throws Exception {
        String folderName = null;
        if (folderEncoded != null && !folderEncoded.trim().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(folderEncoded);
            folderName = new String(decodedBytes);
        }
        try {
            File file = new File(folderName);
            ResourceFileUtils.deleteDir(file);
            return ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

    @PUT
    @Path("/folders/{folderEncoded}/rename/{newName}")
    public Response renameFolderOrFile(@NotNull String folderEncoded, @NotNull String newName) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(folderEncoded);
        String folderName = new String(decodedBytes);
        try {
            File file = new File(folderName);
            String newfolderName = folderName.replace(file.getName(), newName);
            File newFile = new File(newfolderName);
            file.renameTo(newFile);
            return ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

    @POST
    @Path("/folders/{folderEncoded}/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createFile(@Null String folderEncoded, MultipartFormDataInput multipartFormDataInput) {
        File file = null;
        try {
            file = MultipartFormUtils.readFileBytes(multipartFormDataInput, "file");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (file == null) {
            return null;
        }
        try {
            String folderName = null;
            if (folderEncoded != null && !folderEncoded.trim().isEmpty()) {
                byte[] decodedBytes = Base64.getDecoder().decode(folderEncoded);
                folderName = new String(decodedBytes);
            } else {
                folderName = basePaths.get(0);
            }
            Files.write(java.nio.file.Path.of(folderName), FileUtils.readFileToByteArray(file));
            return ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return serverError().build();
    }

}
