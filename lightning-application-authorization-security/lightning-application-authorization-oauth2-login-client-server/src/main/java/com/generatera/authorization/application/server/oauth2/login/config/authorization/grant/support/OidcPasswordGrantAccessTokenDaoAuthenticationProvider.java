package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import com.generatera.authorization.application.server.config.token.AuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.client.authorized.LightningOAuth2AuthorizedClientRepository;
import com.generatera.authorization.application.server.oauth2.login.config.token.PasswordGrantAuthorizationRequestAuthentication;
import com.generatera.authorization.application.server.oauth2.login.config.token.response.LightningOAuth2AccessTokenResponseClient;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Map;

/**
 * 支持 oidc 的 password grant access token dao authentication provider
 *
 * @see com.generatera.authorization.application.server.config.token.LightningDaoAuthenticationProvider
 */
public class OidcPasswordGrantAccessTokenDaoAuthenticationProvider extends PasswordGrantAccessTokenDaoAuthenticationProvider {

    public OidcPasswordGrantAccessTokenDaoAuthenticationProvider(
            LightningOidcUserService oidcUserService,
            LightningOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository
    ) {
        this(
                new DefaultPasswordTokenResponseClient()::getTokenResponse,
                oidcUserService,
                auth2AuthorizedClientRepository
        );
    }

    public OidcPasswordGrantAccessTokenDaoAuthenticationProvider(
            LightningOAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> auth2AccessTokenResponseClient,
            LightningOidcUserService oidcUserService,
            LightningOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository,
            JwtDecoderFactory<ClientRegistration> factory) {
        super(auth2AccessTokenResponseClient,
                new LightningOAuth2UserLoader() {
                    @Override
                    public OAuth2User load(AuthorizationRequestAuthentication authentication, OAuth2AccessTokenResponse auth2AccessTokenResponse) {
                        PasswordGrantAuthorizationRequestAuthentication requestAuthentication = (PasswordGrantAuthorizationRequestAuthentication) authentication;
                        OidcIdToken oidcToken = createOidcToken(requestAuthentication.getClientRegistration(), auth2AccessTokenResponse, factory);
                        return oidcUserService.loadUser(
                                new OidcUserRequest(
                                        requestAuthentication.getClientRegistration(),
                                        auth2AccessTokenResponse.getAccessToken(),
                                        oidcToken,
                                        auth2AccessTokenResponse.getAdditionalParameters()
                                ));
                    }
                }, auth2AuthorizedClientRepository);
    }

    public OidcPasswordGrantAccessTokenDaoAuthenticationProvider(
            LightningOAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> auth2AccessTokenResponseClient,
            LightningOidcUserService oidcUserService,
            LightningOAuth2AuthorizedClientRepository auth2AuthorizedClientRepository) {
        this(auth2AccessTokenResponseClient, oidcUserService, auth2AuthorizedClientRepository, new OidcIdTokenDecoderFactory());
    }

    @Override
    protected void additionalAssert(PasswordGrantAuthorizationRequestAuthentication authentication, OAuth2AccessTokenResponse response) {
        if (!authentication.getAdditionalParameters().containsKey("id_token")) {
            if (!response.getAdditionalParameters().containsKey("id_token")) {
                LightningAuthError invalidIdTokenError = new LightningAuthError("invalid_id_token", "Missing (required) ID Token in Token Response for Client Registration: " + authentication.getClientRegistration().getRegistrationId(), (String) null);
                throw new LightningAuthenticationException(invalidIdTokenError, invalidIdTokenError.toString());
            }
        }
    }

    private static OidcIdToken createOidcToken(ClientRegistration clientRegistration, OAuth2AccessTokenResponse accessTokenResponse, JwtDecoderFactory<ClientRegistration> jwtDecoderFactory) {
        JwtDecoder jwtDecoder = jwtDecoderFactory.createDecoder(clientRegistration);
        Jwt jwt = getJwt(accessTokenResponse, jwtDecoder);
        return new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
    }

    private static Jwt getJwt(OAuth2AccessTokenResponse accessTokenResponse, JwtDecoder jwtDecoder) {
        try {
            Map<String, Object> parameters = accessTokenResponse.getAdditionalParameters();
            return jwtDecoder.decode((String) parameters.get("id_token"));
        } catch (JwtException var5) {
            OAuth2Error invalidIdTokenError = new OAuth2Error("invalid_id_token", var5.getMessage(), null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), var5);
        }
    }

}
