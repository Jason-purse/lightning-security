package com.generatera.authorization.application.server.oauth2.login.config.client.register;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
/**
 * @author FLJ
 * @date 2023/1/9
 * @time 10:00
 * @Description Lightning client registration repository
 *
 * 主要是用于 本身此授权服务器作为其他oauth2中央授权服务器的客户端的存储仓库
 */
public interface LightningOAuth2ClientRegistrationRepository extends ClientRegistrationRepository {

}
