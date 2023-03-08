package com.generatera.authorization.application.server.oauth2.login.config.token;

import com.generatera.authorization.application.server.config.token.AuthorizationRequestAuthentication;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @author FLJ
 * @date 2023/3/7
 * @time 12:48
 * @Description client secret 方法授权请求验证
 */
@Data
@AllArgsConstructor
public class DefaultAuthorizationRequestAuthentication implements AuthorizationRequestAuthentication {

    /**
     * 授权方式 ..
     */
    private String oauth2GrantType;

    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;


    private Map<String,Object> additionalParameters;
}
