package com.generatera.authorization.server.common.configuration.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.generatera.authorization.server.common.configuration.authorization.store.jackson.deserializer.AuthorizationGrantTypeDeserializer;
import com.generatera.authorization.server.common.configuration.authorization.store.jackson.deserializer.OAuth2AuthorizationTokenDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.io.Serializable;
import java.util.Map;

/**
 * @author FLJ
 * @date 2022/12/29
 * @time 16:07
 * @Description OAuth2AuthorizationEntity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedisOAuth2AuthorizationEntity implements Serializable {

    public static final String ACCESS_TOKEN_FIELD_NAME = "accessToken";
    public static final String REFRESH_TOKEN_FIELD_NAME = "refreshToken";
    public static final String OIDC_TOKEN_FIELD_NAME = "oidcToken";
    public static final String AUTHORIZATION_CODE_TOKEN_FIELD_NAME = "authorizationCodeToken";

    /**
     * id
     */
    private String id;

    /**
     * 注册客户端id
     */
    private String registeredClientId;
    /**
     * 主体名称
     */
    private String principalName;
    /**
     * 授权授予类型
     */
    @JsonDeserialize(using = AuthorizationGrantTypeDeserializer.class)
    private AuthorizationGrantType authorizationGrantType;


    @JsonDeserialize(using = OAuth2AuthorizationTokenDeserializer.class)
    private OAuth2Authorization.Token<OAuth2AccessToken> accessToken;

    @Nullable
    @JsonDeserialize(using = OAuth2AuthorizationTokenDeserializer.class)
    private OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken;

    @Nullable
    @JsonDeserialize(using = OAuth2AuthorizationTokenDeserializer.class)
    private OAuth2Authorization.Token<?> oidcToken;

    @Nullable
    @JsonDeserialize(using = OAuth2AuthorizationTokenDeserializer.class)
    private OAuth2Authorization.Token<?> authorizationCodeToken;

    /**
     * 属性(可序列化)
     */
    private Map<String, Object> attributes;
}
