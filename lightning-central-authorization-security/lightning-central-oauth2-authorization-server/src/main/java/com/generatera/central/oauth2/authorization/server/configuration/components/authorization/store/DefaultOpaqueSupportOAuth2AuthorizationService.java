package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.central.oauth2.authorization.server.configuration.components.token.DefaultOpaqueAwareOAuth2TokenCustomizer;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 15:22
 * @Description 实现 OpaqueSupport 感知的 authoirization service ...
 * <p>
 * 默认通过userDetailsService 填充 scopes ...
 * <p>
 * 基于 {@link com.jianyue.lightning.boot.starter.generic.crud.service.support.ThreadLocalSupport} 来保留被丢弃的 scopes ...
 * 进而填充到 Authorization 对象中 ...
 */
public class DefaultOpaqueSupportOAuth2AuthorizationService implements LightningAuthorizationService<DefaultOAuth2Authorization>, OAuth2AuthorizationService {

    private final LightningAuthorizationService<DefaultOAuth2Authorization> delegate;

    private String authoritiesClaimName = "scope";

    private Converter<OAuth2Authorization, OAuth2Authorization.Builder> converter = new Converter<OAuth2Authorization, OAuth2Authorization.Builder>() {
        @Override
        public OAuth2Authorization.Builder convert(OAuth2Authorization source) {
            OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(
                            RegisteredClient.withId(source.getRegisteredClientId())
                                    .clientId(source.getRegisteredClientId()) // fill ...
                                    .build()
                    )
                    .id(source.getId())
                    .principalName(source.getPrincipalName())
                    .attributes(map -> map.putAll(source.getAttributes()))
                    .authorizationGrantType(source.getAuthorizationGrantType())
                    .accessToken(source.getAccessToken().getToken());
            if (source.getRefreshToken() != null) {
                builder
                        .refreshToken(source.getRefreshToken().getToken());
            }
            if (source.getToken(OidcIdToken.class) != null) {
                builder
                        .token(source.getToken(OidcIdToken.class).getToken());
            }
            if (source.getToken(OAuth2AuthorizationCode.class) != null) {
                builder
                        .token(source.getToken(OAuth2AuthorizationCode.class).getToken());
            }

            return builder;
        }
    };


    public DefaultOpaqueSupportOAuth2AuthorizationService(
            LightningAuthorizationService<DefaultOAuth2Authorization> delegate
    ) {
        Assert.notNull(delegate, "oauth2 authorization service must not be null !!!");
        this.delegate = delegate;
    }


    public void setAuthoritiesClaimName(String authoritiesClaimName) {
        Assert.hasText(authoritiesClaimName, "authoritiesClaimName mut not be null !!!");
        this.authoritiesClaimName = authoritiesClaimName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void save(DefaultOAuth2Authorization authorization) {

        // 将刷新token 进行 scope 填充 ..
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null) {
            Map<String, Object> claims = accessToken.getClaims();
            if (claims != null) {
                Object isOpaque = claims.get("isOpaque");
                boolean flag = Boolean.parseBoolean(isOpaque.toString());
                if (flag) {
                    Object authoritiesObj = DefaultOpaqueAwareOAuth2TokenCustomizer.scopeThreadLocal.get();
                    if (authoritiesObj != null) {
                        authorization = new DefaultOAuth2Authorization(
                                OAuth2Authorization.from(authorization.getWrappedAuthorization())
                                        .token(
                                                new OAuth2AccessToken(
                                                        accessToken.getToken().getTokenType(),
                                                        accessToken.getToken().getTokenValue(),
                                                        accessToken.getToken().getIssuedAt(),
                                                        accessToken.getToken().getExpiresAt(),
                                                        ((Set<String>) authoritiesObj)
                                                ),
                                                metaMap -> {
                                                    Object claimsMap = accessToken.getMetadata(OAuth2Authorization.Token.CLAIMS_METADATA_NAME);
                                                    LinkedHashMap<String, Object> claimsData = new LinkedHashMap<>();
                                                    if(claimsMap != null) {
                                                        claimsData = new LinkedHashMap<>((Map<String, Object>)claimsMap);

                                                    }
                                                    // 保存信息
                                                    claimsData.put(authoritiesClaimName,authoritiesObj);
                                                    metaMap.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,claimsData);
                                                }
                                        ).build()
                        );
                    }
                }
            }
        }
        this.delegate.save(authorization);
    }

    @Override
    public void remove(DefaultOAuth2Authorization authorization) {
        this.delegate.remove(authorization);
    }

    @Override
    public DefaultOAuth2Authorization findAuthorizationById(String id) {
        return this.delegate.findAuthorizationById(id);
    }

    @Override
    public DefaultOAuth2Authorization findByToken(String token, LightningTokenType.LightningAuthenticationTokenType tokenType) {
        return this.delegate.findByToken(token, tokenType);
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        this.save(new DefaultOAuth2Authorization(authorization));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        this.delegate.remove(new DefaultOAuth2Authorization(authorization));
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return this.delegate.findAuthorizationById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return this.findByToken(token, tokenType != null ? new LightningTokenType.LightningAuthenticationTokenType(tokenType.getValue()) : null);
    }
}
