package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.application.server.config.authorization.DefaultLightningAuthorization;
import com.generatera.authorization.application.server.config.authorization.store.LightningAuthenticationTokenService;
import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorization;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.TokenSettingsProvider;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContextHolder;
import com.generatera.security.authorization.server.specification.components.token.*;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningRefreshToken;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.ClaimAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 15:19
 * @Description 进行刷新 token的处理 ...
 * <p>
 * 也就是判断刷新token 是否过期,如果没有过期,进行访问token的生成,并刷新刷新token ...
 *
 * 此认证提供器 非常关注 userPrincipal的 状态
 *
 * 简单表单登录 / 和 oauth2 client 登录的刷新 token 抽象提供器 ..
 *
 */
public final class AuthRefreshTokenAuthenticationProvider implements AuthenticationProvider {
    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private final LightningAuthenticationTokenService authorizationService;
    private final LightningTokenGenerator<? extends LightningToken> tokenGenerator;

    private final TokenSettingsProvider tokenSettingsProvider;

    private final LightningUserDetailsProvider userDetailsService;

    public AuthRefreshTokenAuthenticationProvider(LightningAuthenticationTokenService authorizationService,
                                                  LightningTokenGenerator<? extends LightningToken> tokenGenerator,
                                                  TokenSettingsProvider tokenSettingsProvider,
                                                  LightningUserDetailsProvider userDetailsService) {
        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        Assert.notNull(tokenSettingsProvider, "tokenSettingsProvider cannot be null");
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.tokenSettingsProvider = tokenSettingsProvider;
        this.userDetailsService = userDetailsService;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthRefreshTokenAuthenticationToken refreshTokenAuthentication = (AuthRefreshTokenAuthenticationToken) authentication;
        DefaultLightningAuthorization authorization = this.authorizationService.findByToken(refreshTokenAuthentication.getRefreshToken(), LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE);
        if (authorization == null) {
            throw new LightningAuthenticationException("invalid_grant");
        } else if (tokenSettingsProvider.getTokenSettings()
                .getGrantTypes().stream().noneMatch(ele -> ele.value().equals(LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE.value()))) {
            // 不支持刷新的token
            throw new LightningAuthenticationException("un_support_refresh_authorized_token");
        } else {
            DefaultLightningAuthorization.Token<LightningRefreshToken> refreshToken = authorization.getRefreshToken();
            assert refreshToken != null;
            if (!refreshToken.isActive()) {
                throw new LightningAuthenticationException("invalid_refresh_token");
            } else {
                if (!isAuthenticated(authorization.getPrincipal())) {
                    throw new LightningAuthenticationException("unauthorized_grant");
                }

                // 直接获取用户信息,然后重新生成token
                UserDetails userDetails = userDetailsService.getUserDetails(refreshTokenAuthentication,authorization.getPrincipalName());
                if(userDetails == null) {
                    throw new LightningAuthenticationException("invalid_login_grant_type");
                }
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
                // 重新认证
                DefaultLightningTokenContext.Builder builder = DefaultLightningTokenContext.builder()
                        .authentication(authenticationToken)
                        .providerContext(ProviderContextHolder.getProviderContext())
                        .tokenSettings(tokenSettingsProvider.getTokenSettings())
                        .tokenValueType(tokenSettingsProvider.getTokenSettings().getAccessTokenValueType())
                        .tokenIssueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat())
                        .tokenValueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenValueFormat())
                        .tokenType(LightningTokenType.LightningAuthenticationTokenType.ACCESS_TOKEN_TYPE);
                LightningAccessTokenContext accessTokenContext = new LightningAccessTokenContext(
                        builder.build()
                );

                DefaultLightningAuthorization.Builder authorizationBuilder = DefaultLightningAuthorization.from(authorization);

                LightningToken generatedAccessToken = this.tokenGenerator.generate(accessTokenContext);
                if (generatedAccessToken == null) {
                    LightningAuthError error = new LightningAuthError("server_error", "The token generator failed to generate the access token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                    throw new LightningAuthenticationException(error);
                } else {
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
                        authorizationBuilder.token(accessToken, LightningAccessToken.class,(metadata) -> {
                            metadata.put(DefaultLightningAuthorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims());
                            metadata.put(DefaultLightningAuthorization.Token.INVALIDATED_METADATA_NAME, false);
                        });
                    } else {
                        authorizationBuilder.accessToken(accessToken);
                    }


                    LightningRefreshToken currentRefreshToken = refreshToken.getToken();
                    // 如果没有重用刷新 token
                    if (!tokenSettingsProvider.getTokenSettings().isReuseRefreshToken()) {

                        // 刷新 token 处理 ...
                        builder
                                .tokenValueType(tokenSettingsProvider.getTokenSettings().getRefreshTokenValueType())
                                // 暂时没有任何用处 ..
                                .tokenIssueFormat(tokenSettingsProvider.getTokenSettings().getAccessTokenIssueFormat())
                                // todo
                                .tokenValueFormat(tokenSettingsProvider.getTokenSettings().getRefreshTokenValueFormat())
                                .tokenType(LightningTokenType.LightningAuthenticationTokenType.REFRESH_TOKEN_TYPE);

                        LightningToken generatedRefreshToken = this.tokenGenerator.generate(new LightningRefreshTokenContext(builder.build()));
                        if (!(generatedRefreshToken instanceof LightningRefreshToken)) {
                            LightningAuthError error = new LightningAuthError("server_error", "The token generator failed to generate the refresh token.", "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
                            throw new LightningAuthenticationException(error);
                        }

                        currentRefreshToken = (LightningRefreshToken) generatedRefreshToken;
                        authorizationBuilder.refreshToken(currentRefreshToken);
                    }

                    authorizationBuilder.attribute(LightningAuthorization.USER_INFO_ATTRIBUTE_NAME,userDetails);

                    authorization = authorizationBuilder.build();
                    this.authorizationService.save(authorization);
                    Map<String, Object> additionalParameters = Collections.emptyMap();
                    LightningUserPrincipal principal = authorization.getPrincipal();

                    // 创建一个基于username/password authentication token
                    return new AuthAccessTokenAuthenticationToken(
                            new UsernamePasswordAuthenticationToken(
                                    principal, null, principal.getAuthorities()
                            ),
                            accessToken, currentRefreshToken, additionalParameters);
                }
            }
        }
    }

    public boolean supports(Class<?> authentication) {
        return AuthRefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private boolean isAuthenticated(LightningUserPrincipal userPrincipal) {
        return userPrincipal != null && userPrincipal.isAuthenticated();
    }
}