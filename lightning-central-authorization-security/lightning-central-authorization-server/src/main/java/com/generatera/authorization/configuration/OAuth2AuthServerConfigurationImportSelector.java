package com.generatera.authorization.configuration;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 只是为了贴合 开关语义 ..
 * // TODO: 2023/1/2 应该让OAuth2 Auth Server 变得更加优雅 ...
 */
public class OAuth2AuthServerConfigurationImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
    public OAuth2AuthServerConfigurationImportSelector(BeanFactory beanFactory) {
        super(beanFactory, ApplicationAuthServerProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ApplicationAuthServerProperties properties = getProperties();
        ApplicationAuthServerProperties.AuthKind oa2AuthServer = properties.getOA2AuthServer();
        if(oa2AuthServer.isEnable()) {
            return new String[] {OAuth2AuthServerConfiguration.class.getName()};
        }
        return new String[0];
    }
}
