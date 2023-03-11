package com.generatera.authorization.application.server.oauth2.login.config.client.authorized;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

/**
 * oauth2 authorized client service
 *
 * 用来存储 一个已经认证的客户端和用户信息之间的关系(这里获得的访问token等信息,都是最终用户授予它的能力)
 *
 * 主要从容器中获取 ...
 * @see com.generatera.authorization.application.server.oauth2.login.config.ApplicationAuthorizedClientConfiguration
 */
public interface LightningOAuthorizedClientService  extends OAuth2AuthorizedClientService {

}
