package io.snello.model.pojo;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;

public class DocumentFormData {
    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream data;

    @FormParam("uuid")
    @PartType(MediaType.TEXT_PLAIN)
    public String uuid;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    public String filename;

    @FormParam("original_name")
    @PartType(MediaType.TEXT_PLAIN)
    public String original_name;

    @FormParam("originalName")
    @PartType(MediaType.TEXT_PLAIN)
    public String originalName;

    @FormParam("mimeType")
    @PartType(MediaType.TEXT_PLAIN)
    public String mimeType;

    @FormParam("table_name")
    @PartType(MediaType.TEXT_PLAIN)
    public String table_name;

    @FormParam("table_key")
    @PartType(MediaType.TEXT_PLAIN)
    public String table_key;

    public String resolvedOriginalName() {
        if (filename != null && !filename.isBlank()) {
            return filename;
        }
        if (original_name != null && !original_name.isBlank()) {
            return original_name;
        }
        if (originalName != null && !originalName.isBlank()) {
            return originalName;
        }
        return null;
    }
}
