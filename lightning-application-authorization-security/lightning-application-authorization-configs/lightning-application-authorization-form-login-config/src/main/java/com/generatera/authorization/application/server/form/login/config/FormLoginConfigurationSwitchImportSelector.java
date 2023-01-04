package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

public class FormLoginConfigurationSwitchImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {

    public FormLoginConfigurationSwitchImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment, ApplicationAuthServerProperties.class);
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        ApplicationAuthServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();
        if (properties.getFormLogin().isEnable()) {
            candidates.add(ApplicationFormLoginConfiguration.FormLoginConfiguration.class.getName());
        }

        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }
}
