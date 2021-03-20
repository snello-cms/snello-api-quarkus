package io.snello.util;

import io.snello.model.MultipartFormFile;
import io.vertx.core.http.impl.HttpUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInputImpl;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MultipartFormUtils {

    private static final String TO_REMOVE_PNG = "data:image/png;base64,";
    private static final String TO_REMOVE_JPEG = "data:image/jpeg;base64,";

    static Logger logger = Logger.getLogger(MultipartFormUtils.class);

    public static String encodeBase64(File file) throws Exception {
        return encodeBase64(file.getAbsolutePath(), org.apache.commons.io.FileUtils
                .readFileToByteArray(file));
    }

    public static String encodeBase64(String filename, byte[] bytes) throws Exception {
        String enarr = Base64.encodeBase64String(bytes);
        return (filename.toLowerCase().endsWith(".png") ? TO_REMOVE_PNG : TO_REMOVE_JPEG) + enarr;
    }

    public static File readFileBytes(MultipartFormDataInput multipartFormDataInput, String field)
            throws Exception {
        File file = null;

        Map<String, List<InputPart>> formParts = multipartFormDataInput.getFormDataMap();
        List<InputPart> fileParts = formParts.get(field);
        for (InputPart filePart : fileParts) {
            // Retrieve headers, read the Content-Disposition header to obtain the original name of the file
            MultivaluedMap<String, String> headers = filePart.getHeaders();
            String fileName = MimeUtils.getLastPartOf(MimeUtils.parseFileName(headers));
            InputStream istream = filePart.getBody(InputStream.class, null);
            byte[] byteArray = IOUtils.toByteArray(istream);
            file = new File(fileName);
            org.apache.commons.io.FileUtils.writeByteArrayToFile(file, byteArray);
        }
        return file;
    }

    public static InputStream readFileBytesInputStream(MultipartFormDataInput multipartFormDataInput, String field)
            throws Exception {
        Map<String, List<InputPart>> formParts = multipartFormDataInput.getFormDataMap();
        List<InputPart> fileParts = formParts.get(field);
        InputStream istream = null;

        if (fileParts != null) {
            for (InputPart filePart : fileParts) {
                istream = filePart.getBody(InputStream.class, null);
            }
        }

        return istream;
    }

    public static String readTextField(MultipartFormDataInput multipartFormDataInput, String field)
            throws Exception {
        String text = null;

        Map<String, List<InputPart>> formParts = multipartFormDataInput.getFormDataMap();
        List<InputPart> textParts = formParts.get(field);
        if (textParts == null || textParts.isEmpty()) {
            throw new Exception("Field " + field + " not found");
        }
        for (InputPart textPart : textParts) {
            text = textPart.getBodyAsString();
        }
        return text;
    }

    public static String readTextFieldOptional(MultipartFormDataInput multipartFormDataInput, String field)
            throws Exception {
        String text = null;

        Map<String, List<InputPart>> formParts = multipartFormDataInput.getFormDataMap();
        List<InputPart> textParts = formParts.get(field);
        if (textParts == null || textParts.isEmpty()) {
            return null;
        }
        for (InputPart textPart : textParts) {
            text = textPart.getBodyAsString();
        }

        return text;
    }

    public static String extractFieldValue(MultipartFormDataInput multipartFormDataInput, String field)
            throws Exception {
        String text = null;

        Map<String, List<InputPart>> formParts = multipartFormDataInput.getFormDataMap();
        List<InputPart> textParts = formParts.get(field);
        if (textParts != null && !textParts.isEmpty()) {
            for (InputPart textPart : textParts) {
                InputStream in = ((MultipartInputImpl.PartImpl) textPart).getBody();
                int n = in.available();
                byte[] bytes = new byte[n];
                in.read(bytes, 0, n);
                text = new String(bytes, StandardCharsets.UTF_8);
            }
        }

        return text;
    }

    public static MultipartFormFile getMultipartFormFileData(MultipartFormDataInput multipartFormDataInput, String field)
            throws Exception {
        Map<String, List<InputPart>> formParts = multipartFormDataInput.getFormDataMap();
        List<InputPart> fileInputPart = formParts.get(field);

        if (fileInputPart == null) {
            throw new Exception("File must be specified");
        }

        MultipartFormFile multipartFormFile = new MultipartFormFile();
        for (InputPart part : fileInputPart) {
            MultivaluedMap<String, String> headers = part.getHeaders();
            String filename = parseFileName(headers);
            String mimeType = parseMimeType(headers);
            multipartFormFile.setFilename(filename);
            multipartFormFile.setMimeType(mimeType);
        }

        return multipartFormFile;
    }

    public static String parseFileName(MultivaluedMap<String, String> headers) {
        String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
        for (String name : contentDispositionHeader) {
            if ((name.trim().startsWith("filename"))) {
                String[] tmp = name.split("=");
                return tmp[1].trim().replaceAll("(\\|/|\")", "");
            }
        }
        return null;
    }

    public static String parseMimeType(MultivaluedMap<String, String> headers) {
        return headers.getFirst("Content-Type");
    }

}
