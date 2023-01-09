package com.generatera.authorization.application.server.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@AutoConfiguration
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@EnableConfigurationProperties(ApplicationAuthServerProperties.class)
public class ApplicationAuthServerConfig {

    @Bean
    @ConditionalOnMissingBean(AuthExtSecurityConfigurer.class)
    public AuthExtSecurityConfigurer oAuth2ExtSecurityConfigurer(List<LightningAppAuthServerConfigurer> configurers) {
        return new AuthExtSecurityConfigurer(configurers);
    }



    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity,
                                            AuthExtSecurityConfigurer configurer) throws Exception {
        return httpSecurity
                .apply(configurer)
                .and()
                .apply(new SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
                    @Override
                    public void init(HttpSecurity builder) throws Exception {

                        // 最后添加这个
                        builder
                                .authorizeHttpRequests()
                                .anyRequest()
                                .authenticated()
                                .and()
                                .csrf()
                                .disable();
                    }
                })
                .and()
                .build();
    }

}
