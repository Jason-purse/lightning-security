package com.generatera.authorization.configuration;

import com.generatera.authorization.service.UserPrincipalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 此配置启动默认的 基本配置,
 * 1. 例如授权服务器资源保护 .
 * 2. 仅作用户信息获取(授权码 授予)
 * 3. 资源拥有者授予
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(SecurityConfiguration.class);

    @Autowired
    private UserPrincipalService userPrincipalService;

    // If no passwordEncoder bean is defined then you have to prefix password like
    // {noop}secret1, or {bcrypt}password
    // if not static spring boot 2.6.x gives bean currently in creation error at
    // line .passwordEncoder(passwordEncoder()) in configureGlobal() method

    /**
     * @Bean public static PasswordEncoder passwordEncoder() { LOGGER.debug("in
     * passwordEncoder"); return new BCryptPasswordEncoder(); };
     */

    @Autowired
    protected void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
        LOGGER.debug("in configureGlobal");
        builder.userDetailsService(this.userPrincipalService)
                // .passwordEncoder(passwordEncoder())
                .and().eraseCredentials(true);
    }

    // @Bean
    // public WebSecurityCustomizer webSecurityCustomizer() {
    // return (web) -> web.ignoring().requestMatchers("/webjars/**", "/image/**");
    // }

    @Bean
    @Order(Integer.MIN_VALUE)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.debug("configure base security !!!");
        // 联合身份
        //FederatedIdentityConfigurer federatedIdentityConfigurer = new FederatedIdentityConfigurer()
        //        .oauth2UserHandler(new UserRepositoryOAuth2UserHandler());

        return http
                .formLogin()
                .and()
                .authorizeHttpRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint())
                .permitAll()
                .antMatchers("/api/*")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()
                .and()
                .build();
    }




}
