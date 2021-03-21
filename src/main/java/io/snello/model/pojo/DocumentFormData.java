package io.snello.model.pojo;


import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class DocumentFormData {

    @FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] bytes;

    public InputStream data;

    @FormParam("uuid")
    @PartType(MediaType.TEXT_PLAIN)
    public String uuid;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    public String filename;

    @FormParam("mimeType")
    @PartType(MediaType.TEXT_PLAIN)
    public String mimeType;

    @FormParam("table_name")
    @PartType(MediaType.TEXT_PLAIN)
    public String table_name;

    @FormParam("table_key")
    @PartType(MediaType.TEXT_PLAIN)
    public String table_key;
}
