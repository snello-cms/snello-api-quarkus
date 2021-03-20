package io.snello.service.documents.s3;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import io.quarkus.runtime.StartupEvent;
import io.snello.management.AppConstants;
import io.snello.service.documents.DocumentsService;
import io.snello.util.MimeUtils;
import io.snello.util.ResourceFileUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static io.snello.management.AppConstants.*;

@Singleton
//@Requires(property = STORAGE_TYPE, value = "s3")
public class S3Service implements DocumentsService {


    Logger logger = LoggerFactory.getLogger(S3Service.class);

    AmazonS3 s3client;

    @ConfigProperty(name = "amazonwss3.endpoint")
    String s3_endpoint;

    @ConfigProperty(name = "amazonwss3.access_key")
    String s3_access_key;

    @ConfigProperty(name = "amazonwss3.secret_key")
    String s3_secret_key;

    @ConfigProperty(name = "amazonwss3.bucket_name")
    String s3_bucket_name;

    @ConfigProperty(name = S3_BUCKET_FOLDER)
    String s3_bucket_folder;

//
//    public void onStartup(@Observes StartupEvent event) {
//        logger.info("S3Service load");
//        init();
//    }
//
//    public void init() {
//        try {
//            logger.info("s3 s3_endpoint: " + s3_endpoint + ",s3_access_key: " + s3_access_key + ",s3_secret_key: "
//                    + s3_secret_key + ",s3_bucket_name: " + s3_bucket_name);
//            AWSCredentials credentials = new BasicAWSCredentials(
//                    s3_access_key,
//                    s3_secret_key
//            );
//            ClientConfiguration clientConfiguration = new ClientConfiguration();
//            clientConfiguration.setSignerOverride("AWSS3V4SignerType");
//            s3client = AmazonS3ClientBuilder
//                    .standard()
//                    .withEndpointConfiguration(
//                            new AwsClientBuilder.EndpointConfiguration(s3_endpoint, Regions.US_EAST_1.name()))
//                    .withPathStyleAccessEnabled(true)
//                    .withClientConfiguration(clientConfiguration)
//                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                    .build();
//            verificaBucket(s3_bucket_name);
//            verificaFolder();
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }
//
//    private void verificaBucket(String bucket) throws Exception {
//        if (s3client.doesBucketExist(s3_bucket_name)) {
//            logger.info("Bucket already exists.");
//        } else {
//            s3client.createBucket(s3_bucket_name);
//        }
//    }
//
//    private void verificaFolder() throws Exception {
//        logger.info("Bucket verificaFolder: " + s3_bucket_folder);
//        if (s3_bucket_folder != null && !s3_bucket_folder.trim().isEmpty()) {
////            if (!s3_bucket_folder.startsWith("/")) {
////                s3_bucket_folder = "/" + s3_bucket_folder;
////            }
//            if (!s3_bucket_folder.endsWith("/")) {
//                s3_bucket_folder = s3_bucket_folder + "/";
//            }
//            logger.info("Bucket folder: " + s3_bucket_folder);
//        } else {
//            s3_bucket_folder = null;
//        }
//    }
//
//
//    @Override
//    public String basePath(String folder) {
//        if (s3_bucket_folder != null && !s3_bucket_folder.trim().isEmpty()) {
//            return s3_bucket_folder + folder;
//        }
//        return folder;
//    }
//
//    @Override
//    public Map<String, Object> upload(File file, String uuid, String table_name, String table_key) throws Exception {
//        String extension = MimeUtils.getLastPartOf(file.getName());
//        String name = basePath(table_name) + "/" + uuid + "." + extension;
//        Map<String, Object> map = new HashMap<>();
//        map.put(AppConstants.UUID, uuid);
//        map.put(DOCUMENT_NAME, uuid + "." + extension);
//        map.put(DOCUMENT_ORIGINAL_NAME, file.getName());
//        map.put(DOCUMENT_PATH, name);
//        map.put(DOCUMENT_MIME_TYPE, MimeUtils.getContentType(file.getName()));
//        map.put(SIZE, FileUtils.sizeOf(file));
//        map.put(TABLE_NAME, table_name);
//        map.put(TABLE_KEY, table_key);
//        s3client.putObject(
//                s3_bucket_name,
//                name,
//                file);
//        logger.info("document uploaded!");
//        return map;
//    }
//
//    @Override
//    public Map<String, Object> write(File file, String uuid, String table_name) throws Exception {
//        Map<String, Object> map = new HashMap<>();
//        String extension = ResourceFileUtils.getExtension(file.getName());
//        String name = basePath(table_name) + "/" + uuid + "." + extension;
//        s3client.putObject(
//                s3_bucket_name,
//                name,
//                file);
//        map.put(TABLE_NAME, table_name);
//        map.put(DOCUMENT_PATH, name);
//        return map;
//    }
//
//    @Override
//    public Map<String, Object> write(byte[] bytes, String uuid, String table_name, String extension) throws Exception {
//        Map<String, Object> map = new HashMap<>();
//        String name = basePath(table_name) + "/" + uuid + "." + extension;
//        InputStream stream = new ByteArrayInputStream(bytes);
//        int size = bytes.length;
//        String contentType = MimeUtils.getContentType(name);
//        s3client.putObject(
//                s3_bucket_name,
//                name,
//                File);
//        map.put(TABLE_NAME, table_name);
//        map.put(DOCUMENT_PATH, name);
//        return map;
//    }
//
//    @Override
//    public InputStream streamingOutput(String path, String mediatype) throws Exception {
//        S3Object s3obj = s3client.getObject(s3_bucket_name, path);
//        return s3obj.getObjectContent().getDelegateStream();
//    }
//
//
//    @Override
//    public boolean delete(String filename) throws Exception {
//        s3client.doesObjectExist(s3_bucket_name, filename);
//        s3client.deleteObject(s3_bucket_name, filename);
//        return true;
//    }
//
//    @Override
//    public File getFile(String path) throws Exception {
//        String ext = ResourceFileUtils.getExtension(path);
//        File temp = File.createTempFile(java.util.UUID.randomUUID().toString(), ext);
//        InputStream inputStream = s3client.getObject(s3_bucket_name, path).getObjectContent().getDelegateStream();
//        Files.copy(inputStream, temp.toPath());
//        return temp;
//    }
//

}
