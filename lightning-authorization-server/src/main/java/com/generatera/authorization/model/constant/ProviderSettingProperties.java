package com.generatera.authorization.model.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author FLJ
 * @date 2022/12/27
 * @time 10:23
 * @Description 授权服务器 配置属性
 */
@Data
@ConfigurationProperties("lightning.auth.server.provider.settings")
public class ProviderSettingProperties {

    // 可以为空(自己自动生成)
    private String issuer;

    private String authorizationEndpoint = "/auth/v1/oauth2/authorize";

    private String tokenEndpoint = "/auth/v1/oauth2/token";

    private String jwkSetEndpoint = "/auth/v1/oauth2/jwks";

    private String tokenRevocationEndpoint = "/auth/v1/oauth2/revoke";

    private String tokenIntrospectionEndpoint = "/auth/v1/oauth2/introspect";

    private String oidcClientRegistrationEndpoint = "/auth/v1/connect/register";

    private String oidcUserInfoEndpoint = "/auth/v1/userinfo";
}
