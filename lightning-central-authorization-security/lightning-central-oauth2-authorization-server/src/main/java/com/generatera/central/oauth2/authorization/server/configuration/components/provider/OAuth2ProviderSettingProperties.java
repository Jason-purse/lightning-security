package com.generatera.central.oauth2.authorization.server.configuration.components.provider;

import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.util.StringUtils;

/**
 * oauth2 提供器配置 ...
 * 用来调节各个端点的uri
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OAuth2ProviderSettingProperties  extends ProviderSettingProperties {

    public static final String OAUTH2_AUTHORIZATION_ENDPOINT = "/oauth2/authorize";
    public static final String OAUTH2_OIDC_CLIENT_REGISTRATION_ENDPOINT = "/oauth2/register";
    public static final String OAUTH2_OIDC_USER_INFO_ENDPOINT = "/oauth2/userinfo";

    public static final String OAUTH2_JWK_SET_ENDPOINT = "/oauth2/jwks";
    public static final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
    public static final String OAUTH2_TOKEN_REVOCATION_ENDPOINT = "/oauth2/revoke";
    public static final String OAUTH2_TOKEN_INTROSPECTION_ENDPOINT = "/oauth2/introspect";


    private String authorizationEndpoint = OAUTH2_AUTHORIZATION_ENDPOINT;

    private String oidcClientRegistrationEndpoint = OAUTH2_OIDC_CLIENT_REGISTRATION_ENDPOINT;

    private String oidcUserInfoEndpoint = OAUTH2_OIDC_USER_INFO_ENDPOINT;

    {
        setJwkSetEndpoint(OAUTH2_JWK_SET_ENDPOINT);
        setTokenEndpoint(OAUTH2_TOKEN_ENDPOINT);
        setTokenRevocationEndpoint(OAUTH2_TOKEN_REVOCATION_ENDPOINT);
        setTokenIntrospectionEndpoint(OAUTH2_TOKEN_INTROSPECTION_ENDPOINT);
    }

    /**
     * 转换为提供器配置 ..
     */
    public ProviderSettings getOAuth2ProviderSettingsProvider() {

        ProviderSettings.Builder builder = ProviderSettings.builder()
                .authorizationEndpoint(authorizationEndpoint)
                .jwkSetEndpoint(getJwkSetEndpoint())
                .tokenEndpoint(getTokenEndpoint())
                .tokenRevocationEndpoint(getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(getTokenIntrospectionEndpoint())
                .oidcClientRegistrationEndpoint(oidcClientRegistrationEndpoint)
                .oidcUserInfoEndpoint(oidcUserInfoEndpoint);

        if(StringUtils.hasText(getIssuer())) {
            builder.issuer(getIssuer());
        }

        return builder.build();
    }
}
