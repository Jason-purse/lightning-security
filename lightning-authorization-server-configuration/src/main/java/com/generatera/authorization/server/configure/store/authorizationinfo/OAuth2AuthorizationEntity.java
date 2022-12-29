package com.generatera.authorization.server.configure.store.authorizationinfo;

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
public class OAuth2AuthorizationEntity implements Serializable {

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
    private AuthorizationGrantType authorizationGrantType;


    private OAuth2Authorization.Token<OAuth2AccessToken> accessToken;

    @Nullable
    private OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken;

    @Nullable
    private OAuth2Authorization.Token<?> oidcToken;

    @Nullable
    private OAuth2Authorization.Token<?> authorizationCodeToken;

    /**
     * 属性(可序列化)
     */
    private Map<String, Object> attributes;
}
