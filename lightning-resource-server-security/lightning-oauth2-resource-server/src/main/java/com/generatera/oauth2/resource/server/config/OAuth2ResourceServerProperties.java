package com.generatera.oauth2.resource.server.config;

import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@Data
@ConfigurationProperties(OAuth2ResourceServerProperties.OAUTH2_RESOURCE_SERVER_PREFIX)
public class OAuth2ResourceServerProperties {
    public static final String OAUTH2_RESOURCE_SERVER_PREFIX = "lightning.security.oauth2.resource.server";

    /**
     * 重定义, 通过此属性配置 ...
     */
    private OpaqueTokenConfig opaqueTokenConfig = new OpaqueTokenConfig();

    private JwtTokenConfig jwtTokenConfig = new JwtTokenConfig();


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpaqueTokenConfig {

        private  String clientId;

        private  String clientSecret;

        /**
         * 默认 token 校验 url
         */
        private  String introspectTokenEndpointUrl = ProviderSettingProperties.TOKEN_INTROSPECTION_ENDPOINT;

        /**
         * 需要配置
         */
        private List<String> clientMethods;
    }

    @Data
    public static class JwtTokenConfig {

    }
}
