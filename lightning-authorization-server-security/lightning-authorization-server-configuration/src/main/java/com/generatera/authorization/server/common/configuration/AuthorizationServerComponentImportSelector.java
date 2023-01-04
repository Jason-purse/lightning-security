package com.generatera.authorization.server.common.configuration;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;
@Slf4j

public class AuthorizationServerComponentImportSelector extends PropertiesBindImportSelector<AuthorizationServerComponentProperties>{
    public AuthorizationServerComponentImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment,AuthorizationServerComponentProperties.class);
    }
    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        AuthorizationServerComponentProperties properties = getProperties();

        List<String> candidates = new LinkedList<>();


        return candidates.size() > 0 ? candidates.toArray(String[]::new):  new String[0];
    }
}
