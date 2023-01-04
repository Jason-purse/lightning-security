package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;

public class ApplicationOAuth2LoginComponentsImportSelector extends PropertiesBindImportSelector<OAuth2LoginProperties> {
    public ApplicationOAuth2LoginComponentsImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment,OAuth2LoginProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        OAuth2LoginProperties properties = getProperties();

        LinkedList<String> candidates = new LinkedList<>();

        if (properties.getClientRegistrationStoreKind() != null) {
            if(properties.getClientRegistrationStoreKind() == OAuth2LoginProperties.StoreKind.JPA) {
                candidates.add(ApplicationClientRegistrationConfiguration.JPAClientRegistrationConfiguration.class.getName());
            }
            else if(properties.getClientRegistrationStoreKind() == OAuth2LoginProperties.StoreKind.MONGO) {
                candidates.add(ApplicationClientRegistrationConfiguration.MongoClientRegistrationConfiguration.class.getName());
            }
        }

        if(properties.getAuthorizedClientStoreKind() != null) {
            if(properties.getAuthorizedClientStoreKind() == OAuth2LoginProperties.StoreKind.JPA) {
                candidates.add(ApplicationAuthorizedClientConfiguration.JpaAuthorizedClientConfiguration.class.getName());
            }
            else if(properties.getAuthorizedClientStoreKind() == OAuth2LoginProperties.StoreKind.MONGO) {
                candidates.add(ApplicationAuthorizedClientConfiguration.MongoAuthorizedClientConfiguration.class.getName());
            }
        }
        OAuth2LoginProperties.OAuthorizationRequestEndpoint.AuthorizationRequestStoreKind storeKind
                = properties.getAuthorizationRequestEndpoint().getStoreKind();
        if(storeKind != null) {
            if(storeKind == OAuth2LoginProperties.OAuthorizationRequestEndpoint.AuthorizationRequestStoreKind.REDIS) {
                candidates.add(ApplicationAuthorizationRequestConfiguration.RedisAuthorizationRequestConfiguration.class.getName());
            }
            else if(storeKind == OAuth2LoginProperties.OAuthorizationRequestEndpoint.AuthorizationRequestStoreKind.JPA) {
                candidates.add(ApplicationAuthorizedClientConfiguration.JpaAuthorizedClientConfiguration.class.getName());
            }
            else if(storeKind == OAuth2LoginProperties.OAuthorizationRequestEndpoint.AuthorizationRequestStoreKind.MONGO) {
                candidates.add(ApplicationAuthorizationRequestConfiguration.MongoAuthorizationRequestConfiguration.class.getName());
            }
        }

        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }
}
