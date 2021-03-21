package io.snello.producer;

import io.snello.api.service.JdbcRepository;
import io.snello.api.service.StorageService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class StorageProducer {

    @ConfigProperty(name = "snello.storagetype")
    String dbtype;

    public StorageProducer() {
        System.out.println("StorageProducer");
    }

    @Produces
    public StorageService db() throws Exception {
        System.out.println("dbtype: " + dbtype);
        switch (dbtype) {
            case "s3":
                return null;
            case "f2":
                return null;
            case "blobstorage":
                return null;
            default:
                throw new Exception("no dbtype");
        }
    }
}
