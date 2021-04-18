package io.snello.service.producer;

import io.minio.MinioClient;
import io.snello.api.service.StorageService;
import io.snello.service.storage.FsStorageService;
import io.snello.service.storage.S3StorageService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StorageProducer {

    Logger logger = Logger.getLogger(getClass());

    @ConfigProperty(name = "snello.storagetype")
    String storagetype;

    @ConfigProperty(name = "snello.s3.bucketname")
    String bucketName;

    @ConfigProperty(name = "snello.s3.folder")
    String folder;

    @ConfigProperty(name = "snello.fs.basePath")
    String basePath;

    @Inject
    MinioClient minioClient;

    public StorageProducer() {
        System.out.println("StorageProducer");
    }

    @Produces
    public StorageService storage() throws Exception {
        logger.info("storagetype: " + storagetype);
        switch (storagetype) {
            case "s3":
                return new S3StorageService(minioClient, bucketName, folder);
            case "f2":
                return new FsStorageService(basePath);
            case "blobstorage":
                return null;
            default:
                throw new Exception("no storagetype");
        }
    }
}
