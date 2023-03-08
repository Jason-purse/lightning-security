package com.generatera.resource.server.config.util;

import com.generatera.resource.server.config.DataSourceComponent;
import com.mysql.cj.jdbc.Driver;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Builder;
import oracle.jdbc.driver.OracleDriver;
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
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author FLJ
 * @date 2023/2/10
 * @time 16:34
 * @Description 数据库工具类
 *
 */
@Deprecated(since = "暂未使用")
//@Component
@Builder
public class DataBaseUtils {

    private DataSourceProperties dataSourceProperties;

    private JpaProperties jpaProperties;

    private MongoProperties mongoProperties;


    protected void assertDataSourceProperties() {
        Assert.hasText(dataSourceProperties.getUsername(),"username must not be null ");
        Assert.hasText(dataSourceProperties.getPassword(),"password must not be null ");
        Assert.hasText(dataSourceProperties.getDriverClassName(),"driver class name must not be null ");
    }

    protected static <T extends DataSource> T createDataSource(DataSourceProperties properties, Class<T> type) {
        return properties.initializeDataSourceBuilder().type(type).build();
    }

    protected static boolean checkDriverAvailable(String driverName) {
        return ClassUtils.isPresent(driverName,DataBaseUtils.class.getClassLoader());
    }



    public DataSourceComponent getDatabaseSource() throws SQLException {
        DataSource dataSource = getDataSource();
        return DataSourceComponent.JdbcOrOdbcDataSourceComponent.of(dataSource);
    }

    private DataSource getDataSource() throws SQLException {
        assertDataSourceProperties();
        // 尝试根据数据库类型创建
        Class<? extends DataSource> type = dataSourceProperties.getType();
        if (type != null) {
            return dataSourceProperties.initializeDataSourceBuilder()
                    .build();
        }

        // 自动检测并判断是否能池化
        // spring 支持的更加多,但是 我们仅仅支持几种
        // oracle / dbcp2 /hikari / mysql
        // 这个 poolClass 仅仅是检测有没有可用的驱动 ..
        Class<? extends DataSource> pooledClass = DataSourceBuilder.findType(DataBaseUtils.class.getClassLoader());
        // 本身它不会自动检测  mysql ..
        if(pooledClass == null) {
            // 尝试加载mysql
            if(ClassUtils.isPresent("com.mysql.cj.jdbc.MysqlDataSource", DataBaseUtils.class.getClassLoader())) {
                pooledClass = MysqlConnectionPoolDataSource.class;
            }
        }

        // 就算有了 驱动,也先看是否能够池化 ..
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
                // 可以使用jdbc 驱动进行连接
                // 不直接设置驱动
                return Dbcp2.dataSource(dataSourceProperties);
            }

            if (ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource", DataBaseUtils.class.getClassLoader())) {
                // 同上 ..
                return Hikari.dataSource(dataSourceProperties);
            }


            // 兜底处理,直接给出 简单数据源
            // 直接根据设置的驱动和 和数据库类型直接构建一个数据池
            // 可能不支持池化,或者 当前显式支持之外的 池化数据库 ..
            return createDataSource(dataSourceProperties, pooledClass);
        }

        // 否则进行普通处理 ..
        // 例如内嵌数据库 ...
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
            properties.setDriverClassName(
                    OracleDriver.class.getName()
            );
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
            properties.setDriverClassName(Driver.class.getName());
            return createDataSource(properties, MysqlConnectionPoolDataSource.class);
        }
    }

}



