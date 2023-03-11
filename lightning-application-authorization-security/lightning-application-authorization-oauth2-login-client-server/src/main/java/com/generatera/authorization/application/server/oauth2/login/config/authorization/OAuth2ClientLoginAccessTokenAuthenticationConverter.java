package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthenticationConverter;

/**
 * @author FLJ
 * @date 2023/3/10
 * @time 10:00
 * @Description 将前端对 token filter的请求,进行转换为合适的 authentication token,然后由{@link org.springframework.security.authentication.AuthenticationProvider}
 * 进行验证 ...
 */
public interface OAuth2ClientLoginAccessTokenAuthenticationConverter extends LightningAuthenticationConverter {

}
