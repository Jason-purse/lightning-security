package com.generatera.authorization.server.common.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 实现 授权服务器的 /api/** 下的 安全过滤处理 ...
 *
 * 可以覆盖实现,实现更多的功能 ...
 */
@Slf4j
public class AuthExtSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>  {

    private final List<LightningResourcePermissionConfigurer> configurers;


    public AuthExtSecurityConfigurer(List<LightningResourcePermissionConfigurer> configurerList) {
        this.configurers = configurerList;
    }

    @Override
    public void init(HttpSecurity builder) throws Exception {

        ApplicationAuthExtConfigurerUtils.getJwkSourceProvider(builder);

        // todo 后面可以定制 ..
        builder.requestMatchers()
                .antMatchers(AuthConfigConstant.RESOURCE_PATTERN_PREFIX + "/**");

        if(!CollectionUtils.isEmpty(configurers)) {
            for (LightningResourcePermissionConfigurer configurer : configurers) {
                configurer.configure(builder.authorizeHttpRequests());
            }
        }

    }



}
