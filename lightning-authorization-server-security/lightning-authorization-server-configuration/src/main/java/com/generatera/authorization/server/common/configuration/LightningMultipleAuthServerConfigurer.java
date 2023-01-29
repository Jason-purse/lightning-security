package com.generatera.authorization.server.common.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * 每使用一个,则将多增加一个 http security
 * 不要将Order设置为 {@link  Ordered#HIGHEST_PRECEDENCE} 因为我们需要在这些顺序的配置器执行之前 增加应用上下文 ...
 * 查看 {@link  AuthorizationServerCommonComponentsConfiguration#webSecurityCustomizer(ApplicationContext)}
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public interface LightningMultipleAuthServerConfigurer extends WebSecurityCustomizer {

    /**
     * 路由url ..
     * 支持 ant 匹配模式
     */
    String routePattern();
    @Override
    default void customize(WebSecurity web) {
        ApplicationContext context = web.getSharedObject(ApplicationContext.class);
        // 原型bean
        HttpSecurity bean = context.getBean(HttpSecurity.class);
        web.addSecurityFilterChainBuilder(bean);

        bean.requestMatchers()
                        .antMatchers(routePattern());
        try {
            customize(bean);
        }catch (Exception e) {
            // 不可能继续 ..
            throw new RuntimeException(e);
        }
    }

    void customize(HttpSecurity security) throws Exception;
}
