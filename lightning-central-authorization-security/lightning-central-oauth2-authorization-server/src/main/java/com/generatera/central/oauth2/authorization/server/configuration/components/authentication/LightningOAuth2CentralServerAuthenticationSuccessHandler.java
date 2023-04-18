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
 * 它本质上的默认实现是使用 {@link DefaultOAuth2CentralServerAuthenticationSuccessHandler} ...
 *
 * 否则 开发者自己的成功处理器(也就是没有基于此接口进行实现扩展,当然 此框架非常开放,你可以覆盖任何你想要的配置)将没有任何作用 ...
 */
public interface LightningOAuth2CentralServerAuthenticationSuccessHandler extends AuthenticationSuccessHandler {

}
