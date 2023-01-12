package com.generatera.security.authorization.server.specification.components.provider;

import lombok.Data;

@Data
public class ProviderSettingProperties {

    public static final String AUTHORIZATION_ENDPOINT = "/auth/v1/oauth2/authorize";
    public static final String TOKEN_ENDPOINT = "/auth/v1/oauth2/token";
    public static final String JWT_SET_ENDPOINT = "/auth/v1/oauth2/jwks";
    public static final String TOKEN_REVOCATION_ENDPOINT = "/auth/v1/oauth2/revoke";
    public static final String TOKEN_INTROSPECTION_ENDPOINT = "/auth/v1/oauth2/introspect";
    public static final String OIDC_CLIENT_REGISTRATION_ENDPOINT = "/auth/v1/oauth2/register";
    public static final String OIDC_USER_INFO_ENDPOINT = "/auth/v1/oauth2/userinfo";


    // 可以为空(自己自动生成)
    private String issuer;

    private String authorizationEndpoint = AUTHORIZATION_ENDPOINT;

    private String tokenEndpoint = TOKEN_ENDPOINT;

    private String jwkSetEndpoint = JWT_SET_ENDPOINT;

    private String tokenRevocationEndpoint = TOKEN_REVOCATION_ENDPOINT;

    private String tokenIntrospectionEndpoint = TOKEN_INTROSPECTION_ENDPOINT;

    private String oidcClientRegistrationEndpoint = OIDC_CLIENT_REGISTRATION_ENDPOINT;

    private String oidcUserInfoEndpoint = OIDC_USER_INFO_ENDPOINT;
}