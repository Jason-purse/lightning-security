package com.generatera.resource.server.config;


import com.generatera.resource.server.config.method.security.MethodSecurityMetadataSourceConfiguration;
import com.generatera.security.authorization.server.specification.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

public class LightningResourceServerComponentsImportSelector extends PropertiesBindImportSelector<ResourceServerProperties> {

    public LightningResourceServerComponentsImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        List<String> of = new LinkedList<>();

        ResourceServerProperties properties = getProperties();
        methodSecurityMetadataSourceConfiguration(of, properties);
        return of.toArray(String[]::new);
    }

    private void methodSecurityMetadataSourceConfiguration(List<String> of, ResourceServerProperties properties) {
        ResourceServerProperties.AuthorityConfig authorityConfig = properties.getAuthorityConfig();
        ResourceServerProperties.StoreKind resourceAuthoritySaveKind = authorityConfig.getResourceAuthoritySaveKind();
        if(ResourceServerProperties.StoreKind.JPA == resourceAuthoritySaveKind) {
            of.add(MethodSecurityMetadataSourceConfiguration.JapMethodSecurityMetadataSourceConfiguration.class.getName());
        }
    }
}
