package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderHandlerMethodArgumentEnhancer;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalPropertyHandlerMethodArgumentEnhancer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.List;

/**
 * @author FLJ
 * @date 2023/3/3
 * @time 18:16
 * @Description 包含了一些引导性的东西 ...
 * 例如 {@link UserPrincipalPropertyHandlerMethodArgumentEnhancer} 进行{@link  com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalProperty} 的检测
 * 并实现 token中的数据进行自动注入 ..
 * 例如{@link RequestHeaderHandlerMethodArgumentEnhancer} 进行 自动注入 ...
 *
 * 以及{@link  BootstrapContext} 包含了一些方便的类 ..能够解决注入bean的时候,需要一些特定的bean 来实现功能的尴尬问题 ..
 */
@AutoConfiguration
@Configuration
public class LightningSecurityAuthorizationSpecificationAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private final BootstrapContext bootstrapContext = new BootstrapContext();

    @Bean
    UserPrincipalPropertyHandlerMethodArgumentEnhancer userPrincipalPropertyHandlerMethodArgumentEnhancer() {
        return new UserPrincipalPropertyHandlerMethodArgumentEnhancer();
    }

    @Bean
    RequestHeaderHandlerMethodArgumentEnhancer requestHeaderHandlerMethodArgumentResolver() {
        return new RequestHeaderHandlerMethodArgumentEnhancer();
    }

    @Autowired(required = false)
    void setLightningBootstrapContextInitializer(List<LightningBootstrapContextInitializer> initializer) {
        for (LightningBootstrapContextInitializer lightningBootstrapContextInitializer : initializer) {
            lightningBootstrapContextInitializer.initialize(bootstrapContext);
        }
    }

    @Bean
    @Primary
    BootstrapContext bootstrapContext() {
        return bootstrapContext;
    }



    @Bean
    public static LightningBootstrapContextInitializer bootstrapContextInitializer(HttpSecurity httpSecurity) {
        return new LightningBootstrapContextInitializer() {
            @Override
            public void initialize(BootstrapContext bootstrapContext) {
                // 增加HttpSecurity
                bootstrapContext.put(HttpSecurity.class, httpSecurity);
                bootstrapContext.put(ApplicationContext.class,httpSecurity.getSharedObject(ApplicationContext.class));
            }
        };
    }

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        bootstrapContext.clear();
    }
}
