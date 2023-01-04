package com.generatera.authorization.server.oauth2.configuration;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import com.generatera.authorization.server.oauth2.configuration.token.OAuth2TokenComponentConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * oauth2 server 启动配置开关 ..
 */
@Slf4j
public class OAuth2ServerSwitchImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
    public OAuth2ServerSwitchImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment,ApplicationAuthServerProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ApplicationAuthServerProperties properties = getProperties();
        if (properties.getOA2AuthServer().isEnable()) {
            return new String[] {
                    OAuth2ServerCommonComponentsConfiguration.
                            DefaultOAuth2ServerCommonComponentsConfiguration.class.getName(),
                    OAuth2ServerCommonComponentsConfigurationImportSelector.class.getName(),
                    OAuth2TokenComponentConfiguration.class.getName()};
        }
        return new String[0];
    }
}
