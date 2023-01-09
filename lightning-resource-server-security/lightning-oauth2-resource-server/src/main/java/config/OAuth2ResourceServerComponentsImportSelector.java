package config;

import com.generatera.resource.server.config.PropertiesBindImportSelector;
import com.generatera.resource.server.config.ResourceServerProperties;
import com.generatera.resource.server.config.ResourceServerProperties.TokenVerificationConfig;
import config.token.OpaqueTokenVerificationConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedList;
import java.util.List;

public class OAuth2ResourceServerComponentsImportSelector extends PropertiesBindImportSelector<ResourceServerProperties> {

    public OAuth2ResourceServerComponentsImportSelector(BeanFactory beanFactory, Environment environment) {
        super(beanFactory, environment, ResourceServerProperties.class);
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
        if (tokenVerificationConfig.getTokenType() == TokenVerificationConfig.TokenType.Opaque) {
            candidates.add(OpaqueTokenVerificationConfiguration.class.getName());
        }
    }
}
