package com.generatera.resource.server.config;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = ResourceServerProperties.RESOURCE_SERVER_PREFIX)
public class ResourceServerProperties {
    public static final String RESOURCE_SERVER_PREFIX = "lightning.security.resource.server";

    private final TokenVerificationConfig tokenVerificationConfig = new TokenVerificationConfig();

    /**
     * resource url 白名单(如果连同 auth server,那么可以使用authorization server对应的白名单列表控制)
     * {@link AuthorizationServerComponentProperties#getPermission()}
     */
    private final Permission permission = new Permission();

    @Data
    public static class TokenVerificationConfig {
        /**
         * 默认使用Bearer jwt Token
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

    @Data
    public static class Permission {

        private List<String> urlWhiteList;

    }
}
