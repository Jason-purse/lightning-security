package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lightning.resource.server.oauth2.jwt.bearer.token")
public class OAuth2JwtBearerTokenResourceServerProperties {

    private final AuthorizationServerInfoSettings authorizationServerInfoSettings =
            new AuthorizationServerInfoSettings();

    @Data
    public static class AuthorizationServerInfoSettings {

        private String issuer;

    }

}
