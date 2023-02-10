package com.generatera.resource.server.config.util;

import com.generatera.resource.server.config.DataSourceComponent;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Builder;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author FLJ
 * @date 2023/2/10
 * @time 16:34
 * @Description 数据库工具类
 */
@Component
@Builder
public class DataBaseUtils {

    private DataSourceProperties dataSourceProperties;

    private JpaProperties jpaProperties;

    private MongoProperties mongoProperties;


    protected static <T extends DataSource> T createDataSource(DataSourceProperties properties, Class<T> type) {
        return properties.initializeDataSourceBuilder().type(type).build();
    }



    public DataSourceComponent getDatabaseSource() throws SQLException {
        DataSource dataSource = getDataSource();
        return DataSourceComponent.JdbcOrOdbcDataSourceComponent.of(dataSource);
    }

    private DataSource getDataSource() throws SQLException {

        // mysql 应该可以直接创建处理 ...
        Class<? extends DataSource> type = dataSourceProperties.getType();
        if (type != null) {
            return dataSourceProperties.initializeDataSourceBuilder()
                    .build();
        }

        // 判断是否能池化
        Class<? extends DataSource> pooledClass = DataSourceBuilder.findType(DataBaseUtils.class.getClassLoader());
        if (pooledClass != null) {
            // 池化
            if (ClassUtils.isPresent("oracle.ucp.jdbc.PoolDataSource", DataBaseUtils.class.getClassLoader())
                    && ClassUtils.isPresent("oracle.ucp.jdbc.PoolDataSource", DataBaseUtils.class.getClassLoader())
                    && ClassUtils.isPresent("oracle.jdbc.OracleConnection", DataBaseUtils.class.getClassLoader())) {
                return OracleUcp.dataSource(dataSourceProperties);
            }

            if(ClassUtils.isPresent("com.mysql.cj.jdbc.MysqlDataSource",DataBaseUtils.class.getClassLoader())) {
                return Mysql.dataSource(dataSourceProperties);
            }

            if (ClassUtils.isPresent("org.apache.commons.dbcp2.BasicDataSource", DataBaseUtils.class.getClassLoader())) {
                return Dbcp2.dataSource(dataSourceProperties);
            }

            if (ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource", DataBaseUtils.class.getClassLoader())) {
                return Hikari.dataSource(dataSourceProperties);
            }
        }

        EmbeddedDatabaseType embeddedDatabaseType = EmbeddedDatabaseConnection.get(DataBaseUtils.class.getClassLoader()).getType();
        if (embeddedDatabaseType != null) {
            // 是否为内嵌的 ..
            return new EmbeddedDataSourceConfiguration()
                    .dataSource(dataSourceProperties);
        }


        throw new IllegalStateException("please config data source correctly !!!");
    }


    static class OracleUcp {
        OracleUcp() {
        }

        static PoolDataSourceImpl dataSource(DataSourceProperties properties) throws SQLException {
            PoolDataSourceImpl dataSource = createDataSource(properties, PoolDataSourceImpl.class);
            dataSource.setValidateConnectionOnBorrow(true);
            if (StringUtils.hasText(properties.getName())) {
                dataSource.setConnectionPoolName(properties.getName());
            }
            return dataSource;
        }
    }


    static class Dbcp2 {
        Dbcp2() {
        }


        static BasicDataSource dataSource(DataSourceProperties properties) {
            return createDataSource(properties, BasicDataSource.class);
        }
    }


    static class Hikari {

        static HikariDataSource dataSource(DataSourceProperties properties) {
            HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
            if (StringUtils.hasText(properties.getName())) {
                dataSource.setPoolName(properties.getName());
            }

            return dataSource;
        }
    }

    static class Mysql {

        static MysqlDataSource dataSource(DataSourceProperties properties) {
            return createDataSource(properties, MysqlConnectionPoolDataSource.class);
        }
    }

}



