package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderHandlerMethodArgumentResolver;
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
 * @Description
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
    RequestHeaderHandlerMethodArgumentResolver requestHeaderHandlerMethodArgumentResolver() {
        return new RequestHeaderHandlerMethodArgumentResolver();
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
    public LightningBootstrapContextInitializer bootstrapContextInitializer(HttpSecurity httpSecurity) {
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
