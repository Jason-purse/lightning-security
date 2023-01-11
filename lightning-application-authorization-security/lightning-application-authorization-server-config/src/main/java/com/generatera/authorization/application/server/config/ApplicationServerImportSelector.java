package com.generatera.authorization.application.server.config;

import com.jianyue.lightning.boot.starter.util.OptionalFlux;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;
/**
 * @author FLJ
 * @date 2023/1/11
 * @time 15:48
 * @Description 主要原因是 PropertiesBindImportSelector 是此项目提供的 ..
 *
 * 也可以放到authorization server config中 ...
 */
public class ApplicationServerImportSelector extends PropertiesBindImportSelector<ApplicationAuthServerProperties> {
    public ApplicationServerImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment, ApplicationAuthServerProperties.class);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ApplicationAuthServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();

        OptionalFlux.of(properties.getServerMetaDataEndpointConfig().getEnableOidc())
                .consumeOrNull(flag -> {
                    if (flag == null) {
                        candidates.add(AuthServerProviderMetadataConfiguration.NoOidcProviderServerMetadataController.class.getName());
                    } else {
                        if (flag) {
                            candidates.add(AuthServerProviderMetadataConfiguration.OidcProviderServerMetadataEnabler.class.getName());
                        } else {
                            candidates.add(AuthServerProviderMetadataConfiguration.NoOidcProviderServerMetadataController.class.getName());
                        }
                    }
                });


        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }
}
