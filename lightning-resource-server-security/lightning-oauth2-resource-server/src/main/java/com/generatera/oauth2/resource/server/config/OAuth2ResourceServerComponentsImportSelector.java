package com.generatera.oauth2.resource.server.config;

import com.generatera.oauth2.resource.server.config.token.JwtTokenVerificationConfiguration;
import com.generatera.oauth2.resource.server.config.token.OpaqueTokenVerificationConfiguration;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.ResourceServerProperties.TokenVerificationConfig;
import com.generatera.security.authorization.server.specification.PropertiesBindImportSelector;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

public class OAuth2ResourceServerComponentsImportSelector extends PropertiesBindImportSelector<ResourceServerProperties> {

    public OAuth2ResourceServerComponentsImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment);
    }

    @NotNull
    @Override
    public String[] selectImports(@NotNull AnnotationMetadata importingClassMetadata) {
        ResourceServerProperties properties = getProperties();
        List<String> candidates = new LinkedList<>();
        tokenVerificationConfig(properties, candidates);
        return candidates.size() > 0 ? candidates.toArray(String[]::new) : new String[0];
    }

    private static void tokenVerificationConfig(ResourceServerProperties properties, List<String> candidates) {
        TokenVerificationConfig tokenVerificationConfig = properties.getTokenVerificationConfig();
        if(tokenVerificationConfig.getTokenType() != null) {
            if (tokenVerificationConfig.getTokenType() == TokenVerificationConfig.TokenType.Opaque) {
                candidates.add(OpaqueTokenVerificationConfiguration.class.getName());
            }
            else if(tokenVerificationConfig.getTokenType() == TokenVerificationConfig.TokenType.JWT) {
                candidates.add(JwtTokenVerificationConfiguration.class.getName());
            }
        }
        else {
            candidates.add(JwtTokenVerificationConfiguration.class.getName());
        }
    }
}
