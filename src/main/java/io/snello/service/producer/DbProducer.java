package io.snello.service.producer;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.logging.Log;
import io.snello.api.service.JdbcRepository;
import io.snello.service.repository.h2.H2JdbcRepository;
import io.snello.service.repository.mysql.MysqlJdbcRepository;
import io.snello.service.repository.postgresql.PostgresqlJdbcRepository;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class DbProducer {
    
    @ConfigProperty(name = "snello.dbtype", defaultValue = "")
    String dbtype;

    @Inject
    @DataSource("mysql")
    AgroalDataSource mysqlDataSource;

    @Inject
    @DataSource("postgresql")
    AgroalDataSource postgresqlDataSource;


    @Inject
    @DataSource("h2")
    AgroalDataSource h2DataSource;

    public DbProducer() {
        Log.info("DbProducer");
    }

    @Produces
    public JdbcRepository db() throws Exception {
        Log.info("dbtype: " + dbtype);
        switch (dbtype) {
            case "mysql":
                return new MysqlJdbcRepository(mysqlDataSource);
            case "postgresql":
                return new PostgresqlJdbcRepository(postgresqlDataSource);
            case "h2":
                return new H2JdbcRepository(h2DataSource);
            default:
                throw new Exception("no dbtype");
        }
    }
}
