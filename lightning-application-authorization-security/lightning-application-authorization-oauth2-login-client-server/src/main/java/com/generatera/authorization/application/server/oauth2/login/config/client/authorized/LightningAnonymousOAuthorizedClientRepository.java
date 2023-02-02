package com.generatera.authorization.application.server.oauth2.login.config.client.authorized;

import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

/**
 * 匿名用户的 有关授权客户端服务处理
 */
public interface LightningAnonymousOAuthorizedClientRepository extends OAuth2AuthorizedClientRepository {
}
