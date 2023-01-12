package com.generatera.plain.resource.server.config;

import com.generatera.oauth2.resource.server.config.OAuth2ResourceServerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;

/**
 * @author FLJ
 * @date 2023/1/12
 * @time 10:07
 * @Description 简单资源服务器配置 ..
 * <p>
 * 目前先借用 oauth2 resource server
 */
@AutoConfigureBefore(OAuth2ResourceServerConfiguration.class)
public class PlainResourceServerConfiguration {



}
