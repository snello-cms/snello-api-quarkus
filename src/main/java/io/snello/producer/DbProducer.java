package io.snello.producer;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.snello.repository.JdbcRepository;
import io.snello.repository.h2.H2JdbcRepository;
import io.snello.repository.mysql.MysqlJdbcRepository;
import io.snello.repository.postgresql.PostgresqlJdbcRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DbProducer {

    @ConfigProperty(name = "snello.dbtype")
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
        System.out.println("DbProducer");
    }

    @Produces
    public JdbcRepository db() throws Exception {
        System.out.println("dbtype: " + dbtype);
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
