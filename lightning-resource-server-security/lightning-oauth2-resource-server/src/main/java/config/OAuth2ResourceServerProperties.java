package config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static config.OAuth2ResourceServerProperties.OAUTH2_RESOURCE_SERVER_PREFIX;

@Data
@ConfigurationProperties(OAUTH2_RESOURCE_SERVER_PREFIX)
public class OAuth2ResourceServerProperties {
    public static final String OAUTH2_RESOURCE_SERVER_PREFIX = "lightning.security.oauth2.resource.server";




}
