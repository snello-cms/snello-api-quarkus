package io.snello.model;

import java.io.InputStream;

public class MultipartFormFile {

    private String table_name;
    private String table_key;
    private String filename;
    private String size;
    private String mimeType;
    private InputStream inputStream;

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getTable_key() {
        return table_key;
    }

    public void setTable_key(String table_key) {
        this.table_key = table_key;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public MultipartFormFile() {
    }
}
