package com.generatera.authorization.application.server.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.List;

/**
 * @author FLJ
 * @date 2023/1/30
 * @time 9:36
 * @Description 配置应用级的授权服务器 引导
 * 例如{@link  LightningAppAuthServerConfigurer} 被用来配置{@link  ApplicationAuthServerConfigurer}
 * 而此配置器被{@link  com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer} 来配置
 * 应用级服务器的配置引导,例如{@link ApplicationAuthServerConfig#bootstrapAppAuthServer(List)}
 *
 * 所以建议,新增一种认证方式(也就是一种服务器的时候,启用一个的配置器) ..
 *
 * 其他情况下,仅仅使用 {@link LightningAppAuthServerConfigurer} 进行配置应用认证服务器配置 ...
 *
 * 当然此框架设定非常的活,有没有什么特别的约定熟成，也就是说你可以随便改变你想要自定义的部分,只要你对spring-security特别熟悉 ..
 *
 * @see LightningAppAuthServerConfigurer
 */
public interface LightningAppAuthServerBootstrapConfigurer {
    void configure(HttpSecurity security) throws Exception;
}
