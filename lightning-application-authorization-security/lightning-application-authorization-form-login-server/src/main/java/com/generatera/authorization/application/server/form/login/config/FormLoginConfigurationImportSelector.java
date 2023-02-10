package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.application.server.form.login.config.components.BackendSeparationConfiguration;
import com.generatera.security.authorization.server.specification.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 表单登陆 选择配置 ..
 */
public class FormLoginConfigurationImportSelector extends PropertiesBindImportSelector<FormLoginProperties> {
    private final ApplicationAuthServerProperties authServerProperties;

    public FormLoginConfigurationImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment);
        this.authServerProperties = bind(ApplicationAuthServerProperties.class, beanFactory, environment);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        FormLoginProperties properties = getProperties();

        if (authServerProperties.isSeparation()) {
            return new String[]{BackendSeparationConfiguration.class.getName()};
        }
        return new String[0];
    }
}
