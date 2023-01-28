package com.generatera.security.authorization.server.specification.components.provider;

import lombok.Data;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 9:32
 * @Description 普通的 授权服务器 provider 属性配置 ...
 */
@Data
public class ProviderSettingProperties {

    public static final String JWT_SET_ENDPOINT = "/auth/v1/jwks";
    public static final String TOKEN_REVOCATION_ENDPOINT = "/auth/v1/revoke";

    // 默认配置 ..
    public static final String TOKEN_INTROSPECTION_ENDPOINT = "/auth/v1/introspect";
    public static final String TOKEN_ENDPOINT = "/auth/v1/token";

    // 可以为空(自己自动生成)
    private String issuer;

    private String jwkSetEndpoint = JWT_SET_ENDPOINT;

    private String tokenRevocationEndpoint = TOKEN_REVOCATION_ENDPOINT;

    private String tokenIntrospectionEndpoint = TOKEN_INTROSPECTION_ENDPOINT;

    private String tokenEndpoint = TOKEN_ENDPOINT;
}