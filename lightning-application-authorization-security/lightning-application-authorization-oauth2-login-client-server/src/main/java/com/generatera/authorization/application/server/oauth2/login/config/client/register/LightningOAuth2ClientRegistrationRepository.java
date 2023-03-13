package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * @author FLJ
 * @date 2023/1/9
 * @time 10:00
 * @Description Lightning client registration repository
 *
 * 主要是用于 本身此授权服务器作为其他oauth2中央授权服务器的客户端的存储仓库
 *
 * 子类也存在实现Iterable, 实现这个可迭代接口就意味着能够在默认页面上显示登录标签 ... 直接点击标签进行登录 ..
 *
 * 也就是 oauth2 client authorization server 应该实现此接口去扩展  client registration ...
 *
 * 默认情况,这些都作为bean 扫描到spring 容器中了 ...
 */
public interface LightningOAuth2ClientRegistrationRepository extends ClientRegistrationRepository {

}
