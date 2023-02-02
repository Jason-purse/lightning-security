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

    // 当没有多种授权服务器交织的时候.. 它的默认值 ..
    public static final String JWT_SET_ENDPOINT = "/jwks";
    public static final String TOKEN_REVOCATION_ENDPOINT = "/revoke";

    // 默认配置 ..
    public static final String TOKEN_INTROSPECTION_ENDPOINT = "/introspect";
    public static final String TOKEN_ENDPOINT = "/token";

    // 可以为空(自己自动生成)
    private String issuer;


    // 以下属性,在 app auth server中,将自动添加前缀 ...
    private String jwkSetEndpoint = JWT_SET_ENDPOINT;

    private String tokenRevocationEndpoint = TOKEN_REVOCATION_ENDPOINT;

    private String tokenIntrospectionEndpoint = TOKEN_INTROSPECTION_ENDPOINT;

    private String tokenEndpoint = TOKEN_ENDPOINT;
}