package com.generatera.central.oauth2.authorization.server.configuration.components.authentication;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * @author FLJ
 * @date 2023/2/3
 * @time 13:46
 * @Description oauth2 中央服务器认证成功处理器  ...
 *
 * 一般来说,用不上 .... 因为 FormLogin 配置器的默认配置已经够用 ..
 *
 * 这个处理器 应用开发者可以提供去实现自己的 认证成功处理 逻辑 ...
 *
 * 在遵循此框架的 LightningOAuth2CentralServerAuthenticationSuccessHandler 接口 bean 规范,那么
 * 它本质上将会被 {@link DefaultOAuth2CentralServerAuthenticationSuccessHandler} 检测并使用 ...
 *
 * 否则 开发者自己的成功处理器将没有任何作用 ...
 */
public interface LightningOAuth2CentralServerAuthenticationSuccessHandler extends AuthenticationSuccessHandler {

}
