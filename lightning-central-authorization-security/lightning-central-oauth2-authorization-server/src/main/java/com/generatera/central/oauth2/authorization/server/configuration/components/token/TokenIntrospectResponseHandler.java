package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * @author FLJ
 * @date 2023/1/13
 * @time 9:53
 * @Description 处理token 检查之后的响应处理器
 */
public interface TokenIntrospectResponseHandler extends AuthenticationSuccessHandler {
}
