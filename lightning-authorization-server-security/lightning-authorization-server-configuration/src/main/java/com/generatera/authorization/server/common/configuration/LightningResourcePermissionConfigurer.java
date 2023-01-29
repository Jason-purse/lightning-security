package com.generatera.authorization.server.common.configuration;

import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry;
/**
 * @author FLJ
 * @date 2023/1/17
 * @time 15:19
 * @Description 处理权限(url 放行)配置 ...
 * 条件化 配置url list 放行规则 ...
 *
 * 相比于 {@link AuthorizationServerCommonComponentsConfiguration#permissionHandle()},更灵活 ...
 */
public interface LightningResourcePermissionConfigurer {
    void configure(AuthorizationManagerRequestMatcherRegistry registry);
}
