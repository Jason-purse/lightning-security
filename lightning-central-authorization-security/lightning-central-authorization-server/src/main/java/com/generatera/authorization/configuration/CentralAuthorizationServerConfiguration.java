package com.generatera.authorization.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 授权服务器配置
 * <p>
 * 有关oauth2 login,我们需要详细的几个组件 ..
 * 1. 用于查询已经注册的客户端的客户端仓库(RegisteredClientRepository)
 * 2. RegisteredClientService 本质上同 RegisteredClientRepository 一样的作用 ..
 *
 * @author FLJ
 */
@Configuration(proxyBeanMethods = false)
@Import(OAuth2AuthServerConfigurationImportSelector.class)
public class CentralAuthorizationServerConfiguration {

}
