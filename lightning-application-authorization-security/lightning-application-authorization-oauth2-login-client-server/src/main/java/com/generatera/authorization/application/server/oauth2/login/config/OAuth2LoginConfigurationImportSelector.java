//package com.generatera.authorization.application.server.oauth2.login.config;
//
//import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
//import com.generatera.resource.server.config.PropertiesBindImportSelector;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.BeanFactory;
//import org.springframework.core.env.Environment;
//import org.springframework.core.type.AnnotationMetadata;
//
//public class OAuth2LoginConfigurationImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
//
//
//    public OAuth2LoginConfigurationImportSelector(BeanFactory beanFactory, Environment environment) {
//        super(beanFactory, environment,ApplicationAuthServerProperties.class);
//    }
//
//    @NotNull
//    @Override
//    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
//        ApplicationAuthServerProperties properties = getProperties();
//        if (properties.getOauth2Login().isEnable() != null && properties.getOauth2Login().isEnable()) {
//            return new String[] {
//                ApplicationOAuth2LoginConfiguration.OAuth2LoginConfiguration.class.getName()
//            };
//        }
//        return new String[0];
//    }
//}
