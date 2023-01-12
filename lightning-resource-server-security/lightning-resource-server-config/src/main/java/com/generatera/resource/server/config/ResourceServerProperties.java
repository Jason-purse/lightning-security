package com.generatera.resource.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = ResourceServerProperties.RESOURCE_SERVER_PREFIX)
public class ResourceServerProperties {
    public static final String RESOURCE_SERVER_PREFIX = "lightning.security.resource.server";

    private final TokenVerificationConfig tokenVerificationConfig = new TokenVerificationConfig();

    @Data
    public static class TokenVerificationConfig {
        /**
         * 默认使用Bearer Token
         */
        private TokenType tokenType = TokenType.JWT;

        private final BearerTokenConfig bearerTokenConfig = new BearerTokenConfig();

        public enum TokenType {
            JWT,
            Opaque
        }

        @Data
        public static class BearerTokenConfig {
            /**
             *  需不需要 bearer token 前缀 ..
             */
            private Boolean needPrefix = Boolean.TRUE;
        }
    }
}
