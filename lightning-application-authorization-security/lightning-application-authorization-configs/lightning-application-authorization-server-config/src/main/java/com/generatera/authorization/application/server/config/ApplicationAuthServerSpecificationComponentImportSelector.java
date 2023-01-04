package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.specification.authorization.store.AuthenticationTokenComponentConfiguration;
import com.generatera.authorization.server.common.configuration.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/4
 * @time 14:52
 * @Description 遵循 oauth2 部分通用规范的组件的配置选择器 ..
 */
public class ApplicationAuthServerSpecificationComponentImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
    public ApplicationAuthServerSpecificationComponentImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment,ApplicationAuthServerProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {

        ApplicationAuthServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();

        if (properties.getSpecification().getAuthenticationTokenSetting().getEnable()) {
            // 认证 token 存储方式
            ApplicationAuthServerProperties.StoreKind authenticationTokenStoreKind
                    = properties.getSpecification().getAuthenticationTokenSetting().getAuthenticationTokenStoreKind();
            if (authenticationTokenStoreKind != null) {
                if (authenticationTokenStoreKind == ApplicationAuthServerProperties.StoreKind.JPA) {
                    candidates.add(AuthenticationTokenComponentConfiguration.JpaAuthenticationTokenComponentConfiguration.class.getName());
                } else if (authenticationTokenStoreKind == ApplicationAuthServerProperties.StoreKind.MONGO) {
                    candidates.add(AuthenticationTokenComponentConfiguration.MongoAuthenticationTokenComponentConfiguration.class.getName());
                } else if (authenticationTokenStoreKind == ApplicationAuthServerProperties.StoreKind.MEMORY) {
                    candidates.add(AuthenticationTokenComponentConfiguration.MemoryAuthenticationTokenComponentConfiguration.class.getName());
                } else if (authenticationTokenStoreKind == ApplicationAuthServerProperties.StoreKind.REDIS) {
                    candidates.add(AuthenticationTokenComponentConfiguration.RedisAuthenticationTokenComponentConfiguration.class.getName());
                }
            }
        }

        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }
}
