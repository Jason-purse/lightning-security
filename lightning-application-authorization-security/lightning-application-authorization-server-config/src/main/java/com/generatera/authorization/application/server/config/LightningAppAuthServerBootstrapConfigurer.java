package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/**
 * @author FLJ
 * @date 2023/1/30
 * @time 9:36
 * @Description 配置应用级的授权服务器
 */
public interface LightningAppAuthServerBootstrapConfigurer {
    void configure(HttpSecurity security) throws Exception;
}
