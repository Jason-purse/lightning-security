package com.generatera.authorization.application.server.oauth2.login.config;

import com.generatera.authorization.application.server.config.PropertiesBindImportSelector;
import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties.StoreKind;
import org.apache.commons.lang3.ObjectUtils;
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

        // 自己作为客户端的配置考虑
        clientRegistrationConfig(properties, candidates);

        authorizedClientConfig(properties, candidates);


        authorizationRequestConfig(properties, candidates);

        // entry point handled
        if(ObjectUtils.isNotEmpty(properties.getIsSeparation()) && properties.getIsSeparation()) {
            candidates.add(OAuth2LoginAuthenticationEntryPointConfiguration.class.getName());
        }

        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }

    private void authorizedClientConfig(OAuth2LoginProperties properties, LinkedList<String> candidates) {
        if(properties.getAuthorizedClientStoreKind() != null) {
            if(properties.getAuthorizedClientStoreKind() == StoreKind.JPA) {
                candidates.add(ApplicationAuthorizedClientConfiguration.JpaAuthorizedClientConfiguration.class.getName());
            }
            else if(properties.getAuthorizedClientStoreKind() == StoreKind.MONGO) {
                candidates.add(ApplicationAuthorizedClientConfiguration.MongoAuthorizedClientConfiguration.class.getName());
            }
        }
    }

    private static void clientRegistrationConfig(OAuth2LoginProperties properties, LinkedList<String> candidates) {
        if (properties.getClientRegistrationStoreKind() != null) {
            if(properties.getClientRegistrationStoreKind() == StoreKind.JPA) {
                candidates.add(ApplicationClientRegistrationConfiguration.JPAClientRegistrationConfiguration.class.getName());
            }
            else if(properties.getClientRegistrationStoreKind() == StoreKind.MONGO) {
                candidates.add(ApplicationClientRegistrationConfiguration.MongoClientRegistrationConfiguration.class.getName());
            }
        }
    }

    private static void authorizationRequestConfig(OAuth2LoginProperties properties, LinkedList<String> candidates) {
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
    }
}
