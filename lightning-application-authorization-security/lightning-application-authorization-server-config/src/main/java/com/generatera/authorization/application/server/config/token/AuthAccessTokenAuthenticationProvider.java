package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.server.common.configuration.authorization.store.LightningAuthenticationTokenService;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContextHolder;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningRefreshToken;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.ClaimAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

import static com.generatera.security.authorization.server.specification.components.token.LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 17:12
 * @Description auth 授权授予 认证提供器 ...
 * <p>
 * 其实这个就是 password-grant 支持的授权(类似于 oauth2 -password-grant)
 * <p>
 * 不需要交换授权码(所以 如果需要进行登录限制的话,需要做额外的事情)
 * <p>
 * 本质上通过 增强DaoAuthenticationProvider委托它进行 token 生成 ..
 */
public final class AuthAccessTokenAuthenticationProvider implements AppAuthServerForTokenAuthenticationProvider {
    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private static final LightningTokenType.LightningAuthenticationTokenType AUTHORIZATION_GRANT_TYPE = ACCESS_TOKEN_TYPE;
    private final LightningAuthenticationTokenService authorizationService;
    private final LightningTokenGenerator<? extends LightningToken> tokenGenerator;
    private final TokenSettingsProvider tokenSettingsProvider;


    public AuthAccessTokenAuthenticationProvider(
            LightningAuthenticationTokenService authorizationService,
            LightningTokenGenerator<? extends LightningToken> tokenGenerator,
            TokenSettingsProvider tokenSettingsProvider) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(tokenSettingsProvider, "tokenSettingsProvider cannot be null");
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.tokenSettingsProvider = tokenSettingsProvider;
    }

    public AuthAccessTokenAuthenticationToken authenticate(Authentication authentication) throws AuthenticationException {
        AuthAccessTokenAuthenticationToken authAccessTokenAuthenticationToken = (AuthAccessTokenAuthenticationToken) authentication;
        // 不关心authentication中是什么
        DefaultLightningTokenContext.Builder builder = DefaultLightningTokenContext.builder()
                .authentication(authAccessTokenAuthenticationToken.getAuthentication())
                .providerContext(ProviderContextHolder.getProviderContext())
                .tokenSettings(tokenSettingsProvider.getTokenSettings())
                .tokenValueType(tokenSettingsProvider.getTokenSettings().getAccessTokenValueType())
                .tokenIssueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat())
                .tokenValueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenValueFormat())
                .tokenType(LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE);
        LightningToken generatedAccessToken = tokenGenerator.generate(new LightningAccessTokenContext(builder.build()));
        if (generatedAccessToken == null) {
            LightningAuthError error = new LightningAuthError("server_error", "The token generator failed to generate the access token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
            throw new LightningAuthenticationException(error);
        } else {

            DefaultLightningAuthorization authorization;
            DefaultLightningAuthorization.Builder authorizationBuilder = new DefaultLightningAuthorization.Builder();

            authorizationBuilder.principalName(authentication.getName());
            authorizationBuilder.principal(((LightningUserPrincipal) authentication.getPrincipal()));

            LightningAccessToken accessToken
                    = new LightningAccessTokenGenerator.LightningAuthenticationAccessToken(
                    generatedAccessToken,
                    tokenSettingsProvider.getTokenSettings()
                            .getAccessTokenValueType(),
                    tokenSettingsProvider
                            .getTokenSettings()
                            .getAccessTokenValueFormat()
            );
            if (generatedAccessToken instanceof ClaimAccessor) {
                authorizationBuilder.token(accessToken, (metadata) -> {
                    metadata.put(DefaultLightningAuthorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims());
                    metadata.put(DefaultLightningAuthorization.Token.INVALIDATED_METADATA_NAME, false);
                });
            } else {
                authorizationBuilder.accessToken(accessToken);
            }
            LightningToken generatedRefreshToken = null;

            // 是否启用了刷新
            if (tokenSettingsProvider
                    .getTokenSettings()
                    .getGrantTypes().stream().anyMatch(
                            ele ->
                                    ele
                                            .value()
                                            .equalsIgnoreCase(LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE.value())
                    )
            ) {
                // 刷新 token 处理 ...
                builder
                        .tokenValueType(tokenSettingsProvider.getTokenSettings().getRefreshTokenValueType())
                        // 暂时没有任何用处 ..
                        .tokenIssueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat())
                        // todo
                        .tokenValueFormat(tokenSettingsProvider.getTokenSettings().getRefreshTokenValueFormat())
                        .tokenType(LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE);

                generatedRefreshToken = this.tokenGenerator.generate(new LightningRefreshTokenContext(builder.build()));
                if (!(generatedRefreshToken instanceof LightningRefreshToken)) {
                    LightningAuthError error = new LightningAuthError("server_error", "The token generator failed to generate the refresh token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                    throw new LightningAuthenticationException(error);
                }
                authorizationBuilder.refreshToken((LightningRefreshToken) generatedRefreshToken);
            }


            authorization = authorizationBuilder.build();
            this.authorizationService.save(authorization);
            Map<String, Object> additionalParameters = Collections.emptyMap();

            return new AuthAccessTokenAuthenticationToken(
                    authAccessTokenAuthenticationToken.getAuthentication()
                    , accessToken,
                    ((LightningRefreshToken) generatedRefreshToken), additionalParameters);
        }
    }


    public boolean supports(Class<?> authentication) {
        // 通过这个判断,绕过此提供器处理,让父认证提供器处理,然而,在让自己处理 ..
        return AuthAccessTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}