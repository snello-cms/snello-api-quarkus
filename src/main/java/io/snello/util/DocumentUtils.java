package io.snello.util;

import io.snello.model.pojo.DocumentFormData;
import jakarta.ws.rs.BadRequestException;

public final class DocumentUtils {

    private DocumentUtils() {
    }

    public static void validateDocumentFormData(DocumentFormData documentFormData) {
        if (documentFormData == null) {
            throw new BadRequestException("multipart form data is missing");
        }
        if (documentFormData.data == null) {
            throw new BadRequestException("file part is required");
        }
        if (isBlank(documentFormData.resolvedOriginalName())) {
            throw new BadRequestException("filename or original_name is required");
        }
        if (isBlank(documentFormData.mimeType)) {
            throw new BadRequestException("mimeType is required");
        }
        if (isBlank(documentFormData.table_name)) {
            throw new BadRequestException("table_name is required");
        }
        if (isBlank(documentFormData.table_key)) {
            throw new BadRequestException("table_key is required");
        }
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean hasFormatToken(String formats, String target) {
        if (formats == null || formats.isBlank() || target == null || target.isBlank()) {
            return false;
        }
        for (String token : formats.split(",")) {
            if (target.equalsIgnoreCase(token.trim())) {
                return true;
            }
        }
        return false;
    }
}