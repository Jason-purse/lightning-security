package com.generatera;

import com.generatera.resource.server.config.DataSourceComponent;
import com.generatera.resource.server.config.util.DataBaseUtils;
import com.mysql.cj.jdbc.Driver;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class Main {
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

        DataSource dataSource = databaseSource.getDataSource();

        System.out.println(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(new StatementCallback<String>() {
            @Override
            public String doInStatement(Statement stmt) throws SQLException, DataAccessException {
                ResultSet showTables = stmt.executeQuery("show tables");
                ResultSetMetaData metaData = showTables.getMetaData();
                int columnCount = metaData.getColumnCount();
                List<String> titles = new LinkedList<>();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = metaData.getColumnName(i + 1);
                    titles.add(columnName);
                }
                System.out.println(titles);
                while(showTables.next()) {
                    List<String> values = new LinkedList<>();
                    for (int i = 0; i < columnCount; i++) {
                        String object = showTables.getString(i + 1);
                        values.add(object);
                    }

                    System.out.println(values);
                }


                return "success";
            }
        });

        databaseSource.close();
    }
}