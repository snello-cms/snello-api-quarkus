package io.snello.service.rs;

import io.snello.model.pojo.DocumentFormData;
import io.snello.model.pojo.ZipFormData;
import io.snello.service.BackupService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.util.zip.ZipFile;

import static io.snello.management.AppConstants.*;

@Path(BACKUP_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class BackupServiceRs {

    @Inject
    BackupService backupService;

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download() {
        ZipFile output = backupService.exportData();
        return Response.ok(output)
                .header("Content-Disposition", "attachment; filename=\"zipfile.zip\"")
                .build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@MultipartForm ZipFormData zipFormData) {
        backupService.importData(zipFormData.data);
        return Response.ok().build();
    }
}
