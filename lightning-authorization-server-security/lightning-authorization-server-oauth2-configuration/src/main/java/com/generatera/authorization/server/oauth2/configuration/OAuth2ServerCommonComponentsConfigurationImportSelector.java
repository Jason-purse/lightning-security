package com.generatera.authorization.server.oauth2.configuration;

import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import com.generatera.authorization.server.oauth2.configuration.authorization.consent.AuthorizationConsentComponentConfiguration;
import com.generatera.authorization.server.oauth2.configuration.authorization.store.AuthorizationStoreComponentConfiguration;
import com.generatera.authorization.server.oauth2.configuration.client.RegisteredClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

/**
 * 组件 策略化配置
 */
@Slf4j
public class OAuth2ServerCommonComponentsConfigurationImportSelector extends PropertiesBindImportSelector<AuthorizationServerOAuth2CommonComponentsProperties> {
    public OAuth2ServerCommonComponentsConfigurationImportSelector(BeanFactory beanFactory) {
        super(beanFactory, AuthorizationServerOAuth2CommonComponentsProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        AuthorizationServerOAuth2CommonComponentsProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();
        if(properties.getAuthorizationStore().getKind() != null) {
            AuthorizationServerOAuth2CommonComponentsProperties.StoreKind storeKind =
                    properties.getAuthorizationStore().getKind();
            if (storeKind == AuthorizationServerOAuth2CommonComponentsProperties.StoreKind.REDIS) {
                candidates.add(
                        AuthorizationStoreComponentConfiguration.OAuth2AuthorizationStoreConfig.RedisOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == AuthorizationServerOAuth2CommonComponentsProperties.StoreKind.JPA) {
                candidates.add(
                        AuthorizationStoreComponentConfiguration.OAuth2AuthorizationStoreConfig.JpaOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == AuthorizationServerOAuth2CommonComponentsProperties.StoreKind.MONGO) {
                candidates.add(
                        AuthorizationStoreComponentConfiguration.OAuth2AuthorizationStoreConfig.MongoOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
        }

        // 协商存储 ..
        if (properties.getAuthorizationConsentStore().getKind() != null) {
            AuthorizationServerOAuth2CommonComponentsProperties.StoreKind storeKind = properties.getAuthorizationConsentStore().getKind();
            if(storeKind == AuthorizationServerOAuth2CommonComponentsProperties.StoreKind.JPA) {
                candidates.add(AuthorizationConsentComponentConfiguration.JpaAuthorizationConsentComponentConfiguration.class.getName());
            }
            else {
                log.info("current only support JPA authorization Server Consent Component !!!!");
            }
        }

        candidates.add(RegisteredClientConfiguration.class.getName());



        return candidates.size() > 0 ? candidates.toArray(String[]::new) :  new String[0];

    }
}
