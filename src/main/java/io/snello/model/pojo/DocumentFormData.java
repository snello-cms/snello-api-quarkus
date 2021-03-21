package io.snello.model.pojo;


import io.snello.util.MultipartFormUtils;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
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

    @FormParam("mimeType")
    @PartType(MediaType.TEXT_PLAIN)
    public String mimeType;

    @FormParam("table_name")
    @PartType(MediaType.TEXT_PLAIN)
    public String table_name;

    @FormParam("table_key")
    @PartType(MediaType.TEXT_PLAIN)
    public String table_key;

    public DocumentFormData(MultipartFormDataInput partFormData) {
        try {
            this.data = MultipartFormUtils.readFileBytesInputStream(partFormData, "file");
            this.table_name = MultipartFormUtils.readTextField(partFormData, "table_name");
            this.table_key = MultipartFormUtils.readTextField(partFormData, "table_key");
            this.uuid = MultipartFormUtils.readTextField(partFormData, "uuid");
            this.filename = MultipartFormUtils.readTextField(partFormData, "filename");
            this.mimeType = MultipartFormUtils.readTextField(partFormData, "mimeType");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
