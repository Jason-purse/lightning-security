package com.generatera.resource.server.config.test;

import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.method.security.LightningExtMethodSecurityMetadataSource;
import com.generatera.resource.server.config.method.security.MethodSecurityMetadataRepositoryManager;
import org.h2.Driver;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;

public class MethodSecurityMetadataRepositoryManagerTests {
    public static void main(String[] args) throws Exception {

        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
        annotationConfigApplicationContext.refresh();
        ResourceServerProperties resourceServerProperties = new ResourceServerProperties();
        ResourceServerProperties.AuthorityConfiguration.JpaCacheConfig jpaCacheConfig = resourceServerProperties
                .getAuthorityConfig()
                .getCacheConfig().getJpaCacheConfig();
        DataSourceProperties properties = jpaCacheConfig.getDataSourceProperties();
        properties.setUsername("root");
        properties.setPassword("123456");
        properties.setUrl("jdbc:h2:mem:testdb");
        properties.setDriverClassName(Driver.class.getName());

        resourceServerProperties.getAuthorityConfig().setModuleName("method-security-meta-data-fordb-test");

        jpaCacheConfig
                .setEnable(true);
        resourceServerProperties.getAuthorityConfig().setResourceAuthoritySaveKind(ResourceServerProperties.StoreKind.JPA);

        MethodSecurityMetadataRepositoryManager methodSecurityMetadataRepositoryManager = new MethodSecurityMetadataRepositoryManager(
                annotationConfigApplicationContext,
                resourceServerProperties);


        LightningExtMethodSecurityMetadataSource repository = methodSecurityMetadataRepositoryManager.getRepository(
                new ExpressionBasedAnnotationAttributeFactory(
                        new DefaultMethodSecurityExpressionHandler()
                )
        );

        methodSecurityMetadataRepositoryManager.destroy();


        annotationConfigApplicationContext.close();
    }
}
