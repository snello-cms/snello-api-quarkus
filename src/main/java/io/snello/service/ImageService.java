package io.snello.service;


import io.quarkus.logging.Log;
import io.snello.api.service.StorageService;
import io.snello.model.events.ImageEvent;
import io.snello.model.pojo.DocumentFormData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.StreamingOutput;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import static io.snello.management.AppConstants.*;

@ApplicationScoped
public class ImageService {

    private static String table = DOCUMENTS;

    @Inject
    StorageService documentsService;

    @Inject
    ApiService apiService;

    public void onEvent(@ObservesAsync ImageEvent imageEvent) {
        Log.info("resize event " + imageEvent);
        if (imageEvent.isNotValid()) {
            return;
        }
        final String uuid = imageEvent.uuid;
        final String format = imageEvent.format;
        //resize and upload to s3
        resize(uuid, format);
    }

    @Transactional
    public void resize(final String uuid, final String format) {
        //resize and upload to s3
        try {
            Map<String, Object> map = apiService.fetch(null, table, uuid, UUID);
            String path = (String) map.get(DOCUMENT_PATH);
            String mimetype = (String) map.get(DOCUMENT_MIME_TYPE);
            if (map != null) {
                String formats = (String) map.get(FORMATS);
                boolean itemExists = formats != null && !formats.trim().isEmpty() && formats.contains(format);
                if (!itemExists) {
                    String ruuid = uuid + "_" + format;
                    BufferedImage originalImg = downloadImageFromS3(path, mimetype);
                    ByteArrayOutputStream resizedBaos = resize(originalImg, format, mimetype);
                    uploadImageToS3(ruuid, resizedBaos, map);
                    if (formats != null && !formats.trim().isEmpty()) {
                        map.put(FORMATS, formats + "," + format);
                    } else {
                        map.put(FORMATS, format);
                    }
                    apiService.merge(table, map, uuid, UUID);
                }
            }
        } catch (Exception e) {
            Log.error("Failed to create resource in ImageService: " + e);
        }
    }

    private ByteArrayOutputStream resize(BufferedImage originalImage, String format, String mime_type) throws Exception {
        String[] tokens = format.split("x");
        int targetWidth = Integer.valueOf(tokens[0]);
        int targetHeight = Integer.valueOf(tokens[1]);
        String targetImgFormat = getFormatForMimeType(mime_type);
        Log.infov("new width = " + targetWidth + ", new height = " + targetHeight + ", target image format = " + targetImgFormat);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(targetWidth, targetHeight)
                .outputFormat(targetImgFormat)
                .outputQuality(1)
                .toOutputStream(outputStream);
        return outputStream;
    }

    private BufferedImage downloadImageFromS3(String path, String mimetype) throws Exception {
        StreamingOutput output = documentsService.streamingOutput(path, mimetype);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        output.write(baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        BufferedImage originalImage = ImageIO.read(is);
        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();
        Log.infov("image width = " + imageWidth + " image height = " + imageHeight);
        return originalImage;
    }

    private void uploadImageToS3(String uuid, ByteArrayOutputStream resizedBaos, Map<String, Object> map) throws Exception {
        final ByteArrayInputStream resizedBais = new ByteArrayInputStream(resizedBaos.toByteArray());
        DocumentFormData documentFormData = new DocumentFormData();
        documentFormData.data = resizedBais;
        documentFormData.uuid = uuid;
        documentFormData.mimeType = (String) map.get(DOCUMENT_MIME_TYPE);
        documentFormData.filename = (String) map.get(DOCUMENT_NAME);
        documentFormData.table_name = (String) map.get(TABLE_NAME);
        documentFormData.table_key = (String) map.get(TABLE_KEY);
        documentsService.upload(documentFormData);
    }

    private String getFormatForMimeType(String mime_type) throws Exception {
        String[] formats = ImageIO.getWriterFormatNames();
        String lmime_type = mime_type.toLowerCase();
        for (String f : formats) {
            String lf = f.toLowerCase();
            if (lmime_type.indexOf(lf) > 0 || lf.equals(lmime_type))
                return f;
        }
        throw new Exception(String.format("Failed to find Image type for mime type [%s]", mime_type));
    }
}
