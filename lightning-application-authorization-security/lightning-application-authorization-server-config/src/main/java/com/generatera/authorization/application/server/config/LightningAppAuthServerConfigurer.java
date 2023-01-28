package com.generatera.authorization.application.server.config;

import com.generatera.authorization.server.common.configuration.LightningAuthServerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 14:25
 * @Description lightning app auth server configurer ...
 */
public interface LightningAppAuthServerConfigurer extends LightningAuthServerConfigurer {

    @Override
    default void configure(HttpSecurity securityBuilder) throws Exception {

        ApplicationAuthServerConfigurer<HttpSecurity> authServerConfigurer = new ApplicationAuthServerConfigurer<>();
        securityBuilder.apply(authServerConfigurer);
        // 设置为共享对象 ..
        securityBuilder.setSharedObject(ApplicationAuthServerConfigurer.class,authServerConfigurer);

        configure(authServerConfigurer);
    }

    /**
     * 直接配置 ApplicationAuthServerConfigurer 的快捷方式 ...
     * @param applicationAuthServerConfigurer configurer
     * @throws Exception
     */
    default void configure(ApplicationAuthServerConfigurer<HttpSecurity> applicationAuthServerConfigurer) throws Exception {

    }
}
