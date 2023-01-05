package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 实现可以使用oauth2 相关的 访问拒绝处理器 ..
 * BearerTokenAccessDeniedHandler
 * 参考 OAuth2ResourceServerConfigurer 配置类
 */
public interface OAuth2BearerTokenAccessDeniedHandler extends AccessDeniedHandler  {
}
