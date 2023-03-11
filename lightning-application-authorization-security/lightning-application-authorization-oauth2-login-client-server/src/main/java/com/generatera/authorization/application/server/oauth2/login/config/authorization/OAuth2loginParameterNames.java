package com.generatera.authorization.application.server.oauth2.login.config.authorization;
/**
 * @author FLJ
 * @date 2023/3/10
 * @time 15:38
 * @Description oauth2 login parameter names
 *
 * @see org.springframework.security.oauth2.core.AuthorizationGrantType 查看 spring oauth2的支持 ..
 */
public interface OAuth2loginParameterNames {

    /**
     * @see org.springframework.security.oauth2.core.AuthorizationGrantType
     */
    public static final String OAUTH2_GRANT_TYPE =  "oauth2_grant_type";

    String PROVIDER = "provider";
}
