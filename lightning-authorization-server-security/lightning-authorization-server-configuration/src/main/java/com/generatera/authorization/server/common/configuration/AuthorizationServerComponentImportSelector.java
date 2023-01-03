package com.generatera.authorization.server.common.configuration;

import com.generatera.authorization.server.common.configuration.authorization.consent.AuthorizationConsentComponentConfiguration;
import com.generatera.authorization.server.common.configuration.authorization.store.AuthorizationStoreComponentConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;
@Slf4j

public class AuthorizationServerComponentImportSelector extends PropertiesBindImportSelector<AuthorizationServerComponentProperties>{
    public AuthorizationServerComponentImportSelector(BeanFactory beanFactory) {
        super(beanFactory, AuthorizationServerComponentProperties.class);
    }
    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        AuthorizationServerComponentProperties properties = getProperties();

        List<String> candidates = new LinkedList<>();

        if(properties.getAuthorizationStore().getKind() != null) {
            AuthorizationServerComponentProperties.StoreKind storeKind =
                    properties.getAuthorizationStore().getKind();
            if (storeKind == AuthorizationServerComponentProperties.StoreKind.REDIS) {
                candidates.add(
                        AuthorizationStoreComponentConfiguration.OAuth2AuthorizationStoreConfig.RedisOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == AuthorizationServerComponentProperties.StoreKind.JPA) {
                candidates.add(
                        AuthorizationStoreComponentConfiguration.OAuth2AuthorizationStoreConfig.JpaOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == AuthorizationServerComponentProperties.StoreKind.MONGO) {
                candidates.add(
                        AuthorizationStoreComponentConfiguration.OAuth2AuthorizationStoreConfig.MongoOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
        }

        // 协商存储 ..
        if (properties.getAuthorizationConsentStore().getKind() != null) {
            AuthorizationServerComponentProperties.StoreKind storeKind = properties.getAuthorizationConsentStore().getKind();
            if(storeKind == AuthorizationServerComponentProperties.StoreKind.JPA) {
                candidates.add(AuthorizationConsentComponentConfiguration.JpaAuthorizationConsentComponentConfiguration.class.getName());
            }
            else {
                log.info("current only support JPA authorization Server Consent Component !!!!");
            }
        }


        return candidates.size() > 0 ? candidates.toArray(String[]::new):  new String[0];
    }
}
