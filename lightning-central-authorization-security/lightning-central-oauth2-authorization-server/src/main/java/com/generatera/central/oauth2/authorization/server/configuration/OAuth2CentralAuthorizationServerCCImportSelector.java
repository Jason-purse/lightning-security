package com.generatera.central.oauth2.authorization.server.configuration;

import com.generatera.authorization.application.server.config.ApplicationAuthServerProperties;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import com.generatera.authorization.application.server.form.login.config.FormLoginProperties;
import com.generatera.authorization.server.common.configuration.util.LogUtil;
import com.generatera.central.oauth2.authorization.server.configuration.OAuth2CentralAuthorizationServerProperties.StoreKind;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.consent.AuthorizationConsentConfiguration.JpaAuthorizationConsentComponentConfiguration;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.AuthorizationStoreConfiguration;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.AuthorizationStoreConfiguration.JpaOAuth2AuthorizationStoreConfig;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.AuthorizationStoreConfiguration.MongoOAuth2AuthorizationStoreConfig;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.AuthorizationStoreConfiguration.RedisOAuth2AuthorizationStoreConfig;
import com.generatera.central.oauth2.authorization.server.configuration.components.client.RegisteredClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * oauth2 central authorization server 部分组件策略注册
 *
 * 1. client registration
 * 2. authorization consent
 * 3. authorization store
 */
@Slf4j
public class OAuth2CentralAuthorizationServerCCImportSelector extends PropertiesBindImportSelector<OAuth2CentralAuthorizationServerProperties> {
    public OAuth2CentralAuthorizationServerCCImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, configEnvironment(environment));
    }

    private static Environment configEnvironment(Environment environment) {
        if(environment instanceof  ConfigurableEnvironment configurableEnvironment) {
            MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
            LogUtil.prettyLog("OAuth2 Authorization Server requires form login support,so Forms support is built-in, so it is mandatory not to separate configurations[isSeparation = false] !!!");
            propertySources.addFirst(new MapPropertySource(
                    "oauth2-authorization-form-ext-properties",
                    new LinkedHashMap<>() {{
                        put(FormLoginProperties.IS_SEPARATION,Boolean.FALSE);

                        // 设置 oidc 处理,使用oauth2 自带的 oidc
                        // 也就是不需要 ...
                        put(ApplicationAuthServerProperties.ServerMetaDataEndpointConfig.ENABLE_OIDC,Boolean.FALSE);
                    }}
            ));
        }

        return environment;
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        OAuth2CentralAuthorizationServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();
        authorizationStoreConfig(properties, candidates);
        // 协商存储 ..
        authorizationConsentConfig(properties, candidates);

        candidates.add(RegisteredClientConfiguration.ClientRepositoryConfig.class.getName());

        return candidates.size() > 0 ? candidates.toArray(String[]::new) :  new String[0];

    }

    private static void authorizationConsentConfig(OAuth2CentralAuthorizationServerProperties properties, List<String> candidates) {
        if (properties.getAuthorizationConsentStore().getKind() != null) {
            StoreKind storeKind = properties.getAuthorizationConsentStore().getKind();
            if(storeKind == StoreKind.JPA) {
                candidates.add(JpaAuthorizationConsentComponentConfiguration.class.getName());
            }
            else {
                log.info("current only support JPA authorization Server Consent Component !!!!");
            }
        }
    }

    private static void authorizationStoreConfig(OAuth2CentralAuthorizationServerProperties properties, List<String> candidates) {
        if(properties.getAuthorizationStore().getKind() != null) {
            StoreKind storeKind =
                    properties.getAuthorizationStore().getKind();
            if (storeKind == StoreKind.REDIS) {
                candidates.add(
                        RedisOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == StoreKind.JPA) {
                candidates.add(
                        JpaOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == StoreKind.MONGO) {
                candidates.add(
                        MongoOAuth2AuthorizationStoreConfig.class.getName()
                );
            }
            else if(storeKind == StoreKind.MEMORY) {
                candidates.add(AuthorizationStoreConfiguration.MemoryOAuth2AuthorizationStoreConfig.class.getName());
            }
        }
    }
}
