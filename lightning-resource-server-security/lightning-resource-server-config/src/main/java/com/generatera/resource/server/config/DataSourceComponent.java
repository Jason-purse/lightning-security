package com.generatera.resource.server.config;

import com.mongodb.client.MongoClient;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

import javax.sql.DataSource;

public interface DataSourceComponent {

    void close();


    <T> T getDataSource();


    interface JdbcOrOdbcDataSourceComponent extends DataSourceComponent {

        public static JdbcOrOdbcDataSourceComponent of(DataSource dataSource) {
            return new DefaultJdbcOrOdbcDataSourceComponent(dataSource);
        }
    }


    @AllArgsConstructor
    class DefaultJdbcOrOdbcDataSourceComponent implements JdbcOrOdbcDataSourceComponent {

        private final DataSource dataSource;

        @Override
        public void close() {

        }

        @Override
        public <T> T getDataSource() {
            return (T) dataSource;
        }
    }

    interface EmbeddedDataSourceComponent extends DataSourceComponent {

        public static EmbeddedDataSourceComponent of(EmbeddedDatabase database) {
            return new DefaultEmbeddedDataSourceComponent(database);
        }
    }

    @AllArgsConstructor
    class DefaultEmbeddedDataSourceComponent implements EmbeddedDataSourceComponent {

        private final DataSource dataSource;

        @Override
        public void close() {
            ((EmbeddedDatabase) dataSource).shutdown();
        }

        @Override
        public <T> T getDataSource() {
            return (T) null;
        }
    }

    interface MongoDataSourceComponent extends DataSourceComponent {
        public static MongoDataSourceComponent of(MongoClient mongoClient) {
            return new DefaultMongoDataSourceComponent(mongoClient);
        }
    }

    @AllArgsConstructor
    class DefaultMongoDataSourceComponent implements MongoDataSourceComponent {
        private final MongoClient client;

        @Override
        public void close() {
            client.close();
        }

        @Override
        public <T> T getDataSource() {
            return (T) client;
        }
    }
}
