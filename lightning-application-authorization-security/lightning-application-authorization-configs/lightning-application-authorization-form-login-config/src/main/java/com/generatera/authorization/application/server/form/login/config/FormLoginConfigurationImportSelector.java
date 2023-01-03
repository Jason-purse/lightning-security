package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 表单登陆 选择配置 ..
 */
public class FormLoginConfigurationImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
    public FormLoginConfigurationImportSelector(BeanFactory beanFactory) {
        super(beanFactory, ApplicationAuthServerProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ApplicationAuthServerProperties properties = getProperties();
        if (properties.getFormLogin().isEnable()) {
            return new String[]{ApplicationFormLoginConfiguration.FormLoginConfiguration.class.getName()};
        }
        return new String[0];
    }
}
