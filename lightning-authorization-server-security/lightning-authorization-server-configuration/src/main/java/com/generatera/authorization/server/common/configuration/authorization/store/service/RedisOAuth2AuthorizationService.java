package com.generatera.authorization.server.common.configuration.authorization.store.service;

import com.generatera.authorization.server.common.configuration.AuthorizationServerComponentProperties;
import com.generatera.authorization.server.common.configuration.model.entity.RedisOAuth2AuthorizationEntity;
import com.jianyue.lightning.util.JsonUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.security.oauth2.core.OAuth2TokenType.ACCESS_TOKEN;

/**
 * oauth2 authorization service
 * for managing new and existing authorizations.
 * <p>
 * 此redis 存储使用数据格式,Map ...
 */
@AllArgsConstructor
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final RedisTemplate<String, String> redisTemplate;

    private final AuthorizationServerComponentProperties properties;

    private final static String AUTHORIZATION_CODE_TOKEN_TYPE = "authorization_code_token_type";
    private final static String OIDC_TOKEN_TYPE = "oidc_token_type";
    private final static String REFRESH_TOKEN_TYPE = "refresh_token_type";

    private final static String ACCESS_TOKEN_KEY = "access_tokens-";
    private final static String REFRESH_TOKEN_KEY = "refresh_tokens-";
    private final static String OIDC_TOKEN_KEY = "oidc_tokens-";
    private final static String AUTHORIZATION_CODE_TOKEN_KEY = "authorization_code_tokens-";

    @Override
    public void save(OAuth2Authorization authorization) {


        // 主要就是存储tokens ..
        // access token
        final RedisOAuth2AuthorizationEntity entity = RedisOAuth2AuthorizationEntity.builder()
                .id(authorization.getId())
                .principalName(authorization.getPrincipalName())
                .authorizationGrantType(authorization.getAuthorizationGrantType())
                .registeredClientId(authorization.getRegisteredClientId())
                .attributes(authorization.getAttributes())
                .build();

        // 分别存储令牌
        // access token
        final OAuth2AccessToken token = authorization.getAccessToken().getToken();
        final String accessTokenKey = constructKey(token.getTokenType().getValue(), token.getTokenValue());
        entity.setAccessToken(authorization.getAccessToken());
        // but token -> id ref
        tokenSet(ACCESS_TOKEN_KEY,accessTokenKey,authorization.getId());
        // refresh token
        if (authorization.getRefreshToken() != null) {
            final OAuth2RefreshToken refreshToken = authorization.getRefreshToken().getToken();
            final String refreshTokenKey = constructKey(REFRESH_TOKEN_TYPE, refreshToken.getTokenValue());
            entity.setRefreshToken(authorization.getRefreshToken());
            tokenSet(REFRESH_TOKEN_KEY,refreshTokenKey,authorization.getId());
        }

        // oidc id token
        final OAuth2Authorization.Token<OidcIdToken> oidcIdTokenToken = authorization.getToken(OidcIdToken.class);
        if (oidcIdTokenToken != null) {
            final String oidcTokenKey = constructKey(OIDC_TOKEN_TYPE, oidcIdTokenToken.getToken().getTokenValue());
            entity.setOidcToken(oidcIdTokenToken);
            tokenSet(OIDC_TOKEN_KEY,oidcTokenKey,authorization.getId());
        }

        // authorizationCodeToken
        final OAuth2Authorization.Token<OAuth2AuthorizationCode> oAuth2AuthorizationCodeToken =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (oAuth2AuthorizationCodeToken != null) {
            final String authorizationCodeTokenKey = constructKey(AUTHORIZATION_CODE_TOKEN_TYPE,
                    oAuth2AuthorizationCodeToken.getToken().getTokenValue());
            entity.setAuthorizationCodeToken(oAuth2AuthorizationCodeToken);

            tokenSet(AUTHORIZATION_CODE_TOKEN_KEY,authorizationCodeTokenKey,authorization.getId());

        }

        // 构建 key(形成一个用户登录的唯一性约束Key)
        final String key = constructKey(authorization.getId());

        // id -> token entity
        redisTemplate.opsForValue().set(key, JsonUtil.getDefaultJsonUtil().asJSON(entity),properties.getAuthorizationStore().getRedis().getExpiredTimeDuration(), TimeUnit.MILLISECONDS);
    }

    protected String constructKey(Object... name) {
        return Optional.ofNullable(properties.getAuthorizationStore().getRedis().getKeyPrefix())
                .filter(ele -> !StringUtils.isBlank(ele))
                .orElse(properties.getAuthorizationStore().getRedis().getKeyPrefix())
                + StringUtils.joinWith("-", name);

    }

    protected String constructKeyWithId(String id) {
        return constructKey(id);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        redisTemplate.opsForValue().getAndDelete(constructKeyWithId(authorization.getId()));
    }

    @Override
    public OAuth2Authorization findById(String id) {
        Object entity = redisTemplate.opsForValue().get(constructKey(id));
        if (entity != null) {
            RedisOAuth2AuthorizationEntity auth2AuthorizationEntity = JsonUtil.getDefaultJsonUtil().fromJson(entity.toString(), RedisOAuth2AuthorizationEntity.class);
            OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(
                            RegisteredClient.withId(auth2AuthorizationEntity.getRegisteredClientId())
                                    .build()
                    )
                    .id(id)
                    .principalName(auth2AuthorizationEntity.getPrincipalName())
                    .attributes(map -> map.putAll(auth2AuthorizationEntity.getAttributes()))
                    .authorizationGrantType(auth2AuthorizationEntity.getAuthorizationGrantType())
                    .accessToken(auth2AuthorizationEntity.getAccessToken().getToken());
            if (auth2AuthorizationEntity.getRefreshToken() != null) {
                builder
                        .refreshToken(auth2AuthorizationEntity.getRefreshToken().getToken());
            }
            if (auth2AuthorizationEntity.getOidcToken() != null) {
                builder
                        .token(auth2AuthorizationEntity.getOidcToken().getToken());
            }
            if (auth2AuthorizationEntity.getAuthorizationCodeToken() != null) {
                builder
                        .token(auth2AuthorizationEntity.getAuthorizationCodeToken().getToken());
            }

            return builder.build();
        }

        return null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Optional<OAuth2Authorization> result = Optional.empty();
        if (tokenType == null) {
            Object tokenVal = tokenForAccess(token);
            if (tokenVal == null) {
                tokenVal = tokenForRefresh(token);
            }
            if (tokenVal == null) {
                tokenVal = tokenForAuthorizationCode(token);
            }
            if (tokenVal != null) {
                result = Optional.ofNullable(authorizationForId(tokenVal));
            }
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            result = Optional.ofNullable(tokenForAuthorizationCode(token))
                    .map(ele -> authorizationForId(ele.toString()));
        } else if (OAuth2ParameterNames.ACCESS_TOKEN.equals(tokenType.getValue())) {
            result = Optional.ofNullable(tokenForAccess(token))
                    .map(ele -> authorizationForId(ele.toString()));

        } else if (OAuth2ParameterNames.REFRESH_TOKEN.equals(tokenType.getValue())) {
            result = Optional.ofNullable(tokenForRefresh(token))
                    .map(ele -> authorizationForId(ele.toString()));
        }

        return result.orElse(null);
    }

    @Nullable
    private OAuth2Authorization authorizationForId(Object tokenVal) {
        return findById(tokenVal.toString());
    }

    @Nullable
    private Object tokenForAccess(String token) {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY + constructKey(ACCESS_TOKEN.getValue(), token));
    }

    private void tokenSet(String keyPrefix,String key,String id) {
        redisTemplate.opsForValue().set(keyPrefix + key,id,properties.getAuthorizationStore().getRedis().getExpiredTimeDuration(),TimeUnit.MILLISECONDS);
    }

    @Nullable
    private Object tokenForRefresh(String token) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + constructKey(REFRESH_TOKEN_TYPE, token));
    }

    private Object tokenForAuthorizationCode(String token) {
        return redisTemplate.opsForValue().get(AUTHORIZATION_CODE_TOKEN_KEY + constructKey(AUTHORIZATION_CODE_TOKEN_TYPE, token));
    }
}
