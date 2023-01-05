package com.generatera.resource.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 16:17
 * @Description 配置资源服务器的其他配置 ...
 */
public interface LightningResourceServerOtherConfigurer {
    void configure(HttpSecurity httpSecurity);
}
