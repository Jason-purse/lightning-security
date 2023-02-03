package com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.service;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthorizationService;
import com.generatera.central.oauth2.authorization.server.configuration.components.authorization.store.DefaultOAuth2Authorization;
import com.generatera.central.oauth2.authorization.server.configuration.model.entity.authorization.OAuth2AuthorizationRequestEntity;
import com.generatera.central.oauth2.authorization.server.configuration.model.entity.authorization.RedisOAuth2AuthorizationEntity;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.LightningUserPrincipalConverter;
import com.generatera.security.authorization.server.specification.components.token.LightningTokenType;
import com.jianyue.lightning.util.JsonUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * oauth2 authorization service
 * for managing new and existing authorizations.
 * <p>
 * 此redis 存储使用数据格式,Map ...
 * <p>
 * <p>
 * 一般来说,oauth2Authorization store 方式 会选择其中一种进行处理 .
 * 这里不需要做任何兼容,因为没有兼容的必要 ...
 */
@AllArgsConstructor
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService, LightningAuthorizationService<DefaultOAuth2Authorization> {

    private final RedisTemplate<String, String> redisTemplate;

    private final String keyPrefix;

    private final Long expiredDuration;

    private final LightningUserPrincipalConverter userPrincipalConverter;

    private final static String AUTHORIZATION_CODE_TOKEN_TYPE = "authorization_code_token_type";
    private final static String OIDC_TOKEN_TYPE = "oidc_token_type";
    private final static String REFRESH_TOKEN_TYPE = "refresh_token_type";
    private final static String ACCESS_TOKEN_TYPE = "access_token_type";

    private final static String ACCESS_TOKEN_KEY = "access_tokens-";
    private final static String REFRESH_TOKEN_KEY = "refresh_tokens-";
    private final static String OIDC_TOKEN_KEY = "oidc_tokens-";
    private final static String AUTHORIZATION_CODE_TOKEN_KEY = "authorization_code_tokens-";

    private final static String OAUTH2_AUTHORIZATION_REQUEST_ATTRIBUTE = "OAuth2AuthorizationRequest";

    private final Converter<OAuth2AuthorizationRequest, OAuth2AuthorizationRequestEntity> auth2AuthorizationRequestConverter
            = new Converter<OAuth2AuthorizationRequest, OAuth2AuthorizationRequestEntity>() {
        @Override
        public OAuth2AuthorizationRequestEntity convert(@NotNull OAuth2AuthorizationRequest source) {
            return OAuth2AuthorizationRequestEntity
                    .builder()
                    .responseType(source.getResponseType().getValue())
                    .authorizationGrantType(source.getGrantType().getValue())

                    .authorizationUri(source.getAuthorizationUri())
                    .authorizationRequestUri(source.getAuthorizationRequestUri())
                    .additionalParameters(source.getAdditionalParameters())
                    .scopes(source.getScopes())
                    .state(source.getState())
                    .clientId(source.getClientId())
                    .redirectUri(source.getRedirectUri())
                    .attributes(source.getAttributes())
                    .build();
        }
    };

    private final Converter<OAuth2AuthorizationRequestEntity, OAuth2AuthorizationRequest> toAuth2AuthorizationRequestConverter =
            new Converter<OAuth2AuthorizationRequestEntity, OAuth2AuthorizationRequest>() {
                @Override
                public OAuth2AuthorizationRequest convert(@NotNull OAuth2AuthorizationRequestEntity source) {
                    OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode();
                    return builder
                            .authorizationRequestUri(source.getAuthorizationRequestUri())
                            .authorizationUri(source.getAuthorizationUri())
                            .attributes(source.getAttributes())
                            .additionalParameters(source.getAdditionalParameters())
                            .clientId(source.getClientId())
                            .redirectUri(source.getRedirectUri())
                            .scopes(source.getScopes())
                            .state(source.getState())
                            .build();
                }
            };

    /**
     * 转换要求详情查看 {@link org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider}
     */
    private final Converter<Map<String, Object>, Map<String, Object>> attributesConverter
            = new Converter<Map<String, Object>, Map<String, Object>>() {
        @Override
        public Map<String, Object> convert(@NotNull Map<String, Object> source) {
            LinkedHashMap<String, Object> values = new LinkedHashMap<>(source);
            Object oauthRequest = values.remove(OAuth2AuthorizationRequest.class.getName());
            if (oauthRequest != null) {
                OAuth2AuthorizationRequestEntity value = auth2AuthorizationRequestConverter.convert(((OAuth2AuthorizationRequest) oauthRequest));
                values.put(OAUTH2_AUTHORIZATION_REQUEST_ATTRIBUTE, value);
            }

            Object principal = values.remove(Principal.class.getName());
            if(principal != null) {
                // 这里必然是 UserNamePassword ... Authentication ..
                Object principalJson = userPrincipalConverter.serialize(((LightningUserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()));
                values.put(Principal.class.getName(), principalJson);
            }
            return values;
        }
    };

    private final Converter<Map<String, Object>, Map<String, Object>> toAttributesConverter =
            new Converter<Map<String, Object>, Map<String, Object>>() {
                @Override
                @SuppressWarnings("unchecked")
                public Map<String, Object> convert(@NotNull Map<String, Object> source) {
                    LinkedHashMap<String, Object> values = new LinkedHashMap<>(source);
                    Object value = values.remove(OAUTH2_AUTHORIZATION_REQUEST_ATTRIBUTE); // map 对象
                    if (value != null) {
                        OAuth2AuthorizationRequestEntity authorizationRequestEntity = JsonUtil
                                .getDefaultJsonUtil()
                                .convertTo(value, OAuth2AuthorizationRequestEntity.class);
                        OAuth2AuthorizationRequest authorizationRequest = toAuth2AuthorizationRequestConverter.convert(authorizationRequestEntity);
                        values.put(OAuth2AuthorizationRequest.class.getName(), authorizationRequest);
                    }


                    // 处理 java.security.Principal
                    Object principal = values.remove(Principal.class.getName());
                    if(principal != null) {
                        LightningUserPrincipal userPrincipal = userPrincipalConverter.convert(principal);
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userPrincipal, null,
                                userPrincipal.getAuthorities()
                        );

                        values.put(Principal.class.getName(),authenticationToken);
                    }

                    Object scopes = values.remove(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME);

                    // OAuth2AuthorizationCodeAuthenticationProvider 在生成 token 上下文的时候, 需要获取这个属性,并且是Set集合 ..
                    /**
                     * {@link org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider#authenticate(Authentication)}*
                     * {@link DefaultOAuth2TokenContext#getAuthorizedScopes()}
                     *  @see DefaultOAuth2TokenContext ...
                     */
                    // arraylist -> set
                    if(scopes != null) {
                        values.put(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME,new LinkedHashSet<>(((Collection<String>) scopes)));
                    }
                    return values;
                }
            };


    @Override
    public void save(OAuth2Authorization authorization) {


        // 主要就是存储tokens ..
        // access token
        final RedisOAuth2AuthorizationEntity entity = RedisOAuth2AuthorizationEntity.builder()
                .id(authorization.getId())
                .principalName(authorization.getPrincipalName())
                .authorizationGrantType(authorization.getAuthorizationGrantType())
                .registeredClientId(authorization.getRegisteredClientId())
                // 属性转换
                .attributes(attributesConverter.convert(authorization.getAttributes()))
                .build();


        // 分别存储令牌
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (accessToken != null) {
            // access token
            final OAuth2AccessToken token = accessToken.getToken();
            final String accessTokenKey = constructKey(ACCESS_TOKEN_TYPE, token.getTokenValue());
            entity.setAccessToken(authorization.getAccessToken());
            // but token -> id ref
            tokenSet(ACCESS_TOKEN_KEY, accessTokenKey, authorization.getId());
        }

        // refresh token
        if (authorization.getRefreshToken() != null) {
            final OAuth2RefreshToken refreshToken = authorization.getRefreshToken().getToken();
            final String refreshTokenKey = constructKey(REFRESH_TOKEN_TYPE, refreshToken.getTokenValue());
            entity.setRefreshToken(authorization.getRefreshToken());
            tokenSet(REFRESH_TOKEN_KEY, refreshTokenKey, authorization.getId());
        }

        // oidc id token
        final OAuth2Authorization.Token<OidcIdToken> oidcIdTokenToken = authorization.getToken(OidcIdToken.class);
        if (oidcIdTokenToken != null) {
            final String oidcTokenKey = constructKey(OIDC_TOKEN_TYPE, oidcIdTokenToken.getToken().getTokenValue());
            entity.setOidcToken(oidcIdTokenToken);
            tokenSet(OIDC_TOKEN_KEY, oidcTokenKey, authorization.getId());
        }

        // authorizationCodeToken
        final OAuth2Authorization.Token<OAuth2AuthorizationCode> oAuth2AuthorizationCodeToken =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (oAuth2AuthorizationCodeToken != null) {
            final String authorizationCodeTokenKey = constructKey(AUTHORIZATION_CODE_TOKEN_TYPE,
                    oAuth2AuthorizationCodeToken.getToken().getTokenValue());
            entity.setAuthorizationCodeToken(oAuth2AuthorizationCodeToken);

            tokenSet(AUTHORIZATION_CODE_TOKEN_KEY, authorizationCodeTokenKey, authorization.getId());

        }

        // 构建 key(形成一个用户登录的唯一性约束Key)
        final String key = constructKey(authorization.getId());

        // id -> token entity
        redisTemplate.opsForValue().set(key, JsonUtil.getDefaultJsonUtil().asJSON(entity), expiredDuration, TimeUnit.MILLISECONDS);
    }

    protected String constructKey(Object... name) {
        return keyPrefix + StringUtils.joinWith("-", name);

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
            RedisOAuth2AuthorizationEntity auth2AuthorizationEntity = JsonUtil.getDefaultJsonUtil()
                    .fromJson(entity.toString(), RedisOAuth2AuthorizationEntity.class);

            Map<String, Object> attributes = toAttributesConverter.convert(auth2AuthorizationEntity.getAttributes());
            assert attributes != null;
            OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(
                            // 这里的 id 没有任何用处 ..
                            RegisteredClient
                                    .withId(auth2AuthorizationEntity.getRegisteredClientId())
                                    .clientId(auth2AuthorizationEntity.getRegisteredClientId())
                                    .authorizationGrantType(auth2AuthorizationEntity.getAuthorizationGrantType())
                                    // fill, no use
                                    .redirectUri("http://localhost:8080")
                                    .build()
                    )
                    .id(id)
                    .principalName(auth2AuthorizationEntity.getPrincipalName())
                    //  to 属性转换
                    .attributes(map -> map.putAll(attributes))
                    .authorizationGrantType(auth2AuthorizationEntity.getAuthorizationGrantType());

            if (auth2AuthorizationEntity.getAccessToken() != null) {
                builder.accessToken(auth2AuthorizationEntity.getAccessToken().getToken());
            }
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


    @Override
    public void save(DefaultOAuth2Authorization authorization) {
        save(((OAuth2Authorization) authorization));
    }

    @Override
    public void remove(DefaultOAuth2Authorization authorization) {
        remove(((OAuth2Authorization) authorization));
    }

    @Override
    public DefaultOAuth2Authorization findAuthorizationById(String id) {
        OAuth2Authorization authorization = findById(id);
        return authorization != null ? new DefaultOAuth2Authorization(authorization) : null;
    }

    @Override
    public DefaultOAuth2Authorization findByToken(String token, LightningTokenType.LightningAuthenticationTokenType tokenType) {
        OAuth2Authorization authorization = findByToken(token, tokenType != null ? new OAuth2TokenType(tokenType.value()) : null);
        if (authorization != null) {
            return new DefaultOAuth2Authorization(authorization);
        }
        return null;
    }

    @Nullable
    private OAuth2Authorization authorizationForId(Object tokenVal) {
        return findById(tokenVal.toString());
    }

    @Nullable
    private Object tokenForAccess(String token) {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY + constructKey(ACCESS_TOKEN_TYPE, token));
    }

    private void tokenSet(String keyPrefix, String key, String id) {
        redisTemplate.opsForValue().set(keyPrefix + key, id, expiredDuration, TimeUnit.MILLISECONDS);
    }

    @Nullable
    private Object tokenForRefresh(String token) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + constructKey(REFRESH_TOKEN_TYPE, token));
    }

    private Object tokenForAuthorizationCode(String token) {
        return redisTemplate.opsForValue().get(AUTHORIZATION_CODE_TOKEN_KEY + constructKey(AUTHORIZATION_CODE_TOKEN_TYPE, token));
    }
}
