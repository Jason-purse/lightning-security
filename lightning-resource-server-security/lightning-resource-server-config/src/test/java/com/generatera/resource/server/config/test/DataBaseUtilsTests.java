package com.generatera.resource.server.config.test;

import com.generatera.resource.server.config.DataSourceComponent;
import com.generatera.resource.server.config.util.DataBaseUtils;
import com.mysql.cj.jdbc.Driver;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import java.sql.SQLException;

public class DataBaseUtilsTests {

    public static void main(String[] args) throws SQLException {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setDriverClassName(Driver.class.getName());
        dataSourceProperties.setUsername("root");
        dataSourceProperties.setPassword("6tfc^YHN");
        dataSourceProperties.setUrl("jdbc:mysql://company.generatech.ltd:3006/authorization_server_ga?useUnicode=true&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false");

        DataBaseUtils build = DataBaseUtils.builder()
                .dataSourceProperties(dataSourceProperties)
                .build();
        DataSourceComponent databaseSource = build.getDatabaseSource();

        Object dataSource = databaseSource.getDataSource();

        System.out.println(dataSource != null);

        databaseSource.close();
    }
}
