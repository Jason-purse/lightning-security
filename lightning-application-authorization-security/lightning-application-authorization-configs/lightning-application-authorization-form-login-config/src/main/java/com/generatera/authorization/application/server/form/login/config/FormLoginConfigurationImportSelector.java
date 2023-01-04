package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 表单登陆 选择配置 ..
 */
public class FormLoginConfigurationImportSelector extends PropertiesBindImportSelector<FormLoginProperties> {
    public FormLoginConfigurationImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment, FormLoginProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        FormLoginProperties properties = getProperties();
        if (properties.getIsSeparation()) {
            return new String[]{BackendSeparationConfiguration.class.getName()};
        }
        return new String[0];
    }
}
