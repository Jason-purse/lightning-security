package com.generatera.authorization.application.server.form.login.config;

import com.generatera.authorization.application.server.config.ApplicationAuthServerConfig;
import com.generatera.authorization.application.server.config.LightningFormLoginConfigurer;
import com.generatera.authorization.application.server.config.RedirectAuthenticationSuccessOrFailureHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 表单登陆配置
 */
@Configuration
@AutoConfigureAfter(ApplicationAuthServerConfig.class)
@Import(FormLoginConfigurationImportSelector.class)
public class ApplicationFormLoginConfiguration {

    /**
     *  仅当实际需要的时候才会倒入 ..
     */
    @EnableConfigurationProperties(FormLoginProperties.class)
    @RequiredArgsConstructor
    public static class FormLoginConfiguration {

       private final FormLoginProperties formLoginProperties;

       @Bean
       @ConditionalOnMissingBean({AuthenticationSuccessHandler.class})
       public AuthenticationSuccessHandler authenticationSuccessHandler() {
           return lightningFormLoginAuthenticationEntryPoint();
       }

       @Bean
       @ConditionalOnMissingBean({AuthenticationFailureHandler.class})
       public AuthenticationFailureHandler authenticationFailureHandler() {
           return lightningFormLoginAuthenticationEntryPoint();
       }

       /**
        * 代理此方法 ..
        * @return authentication success / failure 都是同一个对象
        */
       private LightningFormLoginAuthenticationEntryPoint lightningFormLoginAuthenticationEntryPoint() {

           LightningFormLoginAuthenticationEntryPoint point = new LightningFormLoginAuthenticationEntryPoint();
           FormLoginProperties.BackendSeparation backendSeparation = formLoginProperties.getBackendSeparation();

           if(StringUtils.hasText(backendSeparation.getLoginSuccessMessage())) {
               point.setLoginSuccessMessage(backendSeparation.getLoginSuccessMessage());
           }

           point.setEnableAccountStatusInform(backendSeparation.getEnableAccountStatusInform());

           if(ObjectUtils.isNotEmpty(backendSeparation.getEnableAccountStatusInform()) && backendSeparation.getEnableAccountStatusInform()) {
               if(StringUtils.hasText(backendSeparation.getAccountLockedMessage())) {
                   point.setAccountStatusLockedMessage(backendSeparation.getAccountLockedMessage());
               }
               if(StringUtils.hasText(backendSeparation.getAccountExpiredMessage())) {
                   point.setAccountStatusExpiredMessage(backendSeparation.getAccountExpiredMessage());
               }
           }

           if(StringUtils.hasText(backendSeparation.getAccountStatusMessage())) {
               point.setAccountStatusMessage(backendSeparation.getAccountStatusMessage());
           }

           if(StringUtils.hasText(backendSeparation.getBadCredentialMessage())) {
               point.setBadCredentialsMessage(backendSeparation.getBadCredentialMessage());
           }

           if(StringUtils.hasText(backendSeparation.getLoginFailureMessage())) {
               point.setLoginFailureMessage(backendSeparation.getLoginFailureMessage());
           }

           return point;
       }


       @Bean
       public LightningFormLoginConfigurer lightningFormLoginConfigurer(
               AuthenticationSuccessHandler authenticationSuccessHandler,
               AuthenticationFailureHandler authenticationFailureHandler) {
           return new LightningFormLoginConfigurer() {
               @Override
               public void configure(FormLoginConfigurer<HttpSecurity> formLoginConfigurer) {

                   List<String> patterns = new LinkedList<>();
                   // 如果是前后端分离的 ..
                   if (formLoginProperties.getIsSeparation()) {
                       // 前后端分离的 handler 配置 ..
                       formLoginConfigurer.successHandler(authenticationSuccessHandler);
                       formLoginConfigurer.failureHandler(authenticationFailureHandler);
                   }
                   else {
                       // 前后端不分离配置 ..
                       FormLoginProperties.NoSeparation noSeparation = formLoginProperties.getNoSeparation();
                       if(StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                           formLoginConfigurer.loginPage(noSeparation.getLoginPageUrl());
                       }

                       if(noSeparation.getEnableSavedRequestForward() != null && noSeparation.getEnableSavedRequestForward()) {
                           if(StringUtils.hasText(noSeparation.getDefaultSuccessUrl())) {
                               formLoginConfigurer.defaultSuccessUrl(noSeparation.getDefaultSuccessUrl());
                               patterns.add(noSeparation.getDefaultSuccessUrl());
                           }
                       }
                       else {
                           // 针对于 转发url 是post请求,所以静态资源不支持,会报错 405
                           // 所以需要自己重写 登陆成功的跳转地址 ..
                           if(noSeparation.getEnableForward() != null && noSeparation.getEnableForward()) {
                               if(StringUtils.hasText(noSeparation.getSuccessForwardOrRedirectUrl())) {
                                   formLoginConfigurer.successForwardUrl(noSeparation.getSuccessForwardOrRedirectUrl());
                                   patterns.add(noSeparation.getSuccessForwardOrRedirectUrl());
                               }

                               if(StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                                   formLoginConfigurer.failureForwardUrl(noSeparation.getFailureForwardOrRedirectUrl());
                                   patterns.add(noSeparation.getFailureForwardOrRedirectUrl());
                               }
                           }
                           else {
                               // 如果都没有填写转发地址 ..
                               // 这里直接启用 强制重定向到 /
                               if(StringUtils.hasText(noSeparation.getSuccessForwardOrRedirectUrl())) {
                                   formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getSuccessForwardOrRedirectUrl()));
                                   patterns.add(noSeparation.getSuccessForwardOrRedirectUrl());
                               }
                               else {
                                   if(!noSeparation.getEnableSavedRequestForward()) {
                                       formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler("/"));
                                       patterns.add("/");
                                   }
                               }

                               if(StringUtils.hasText(noSeparation.getFailureForwardOrRedirectUrl())) {
                                   formLoginConfigurer.successHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getFailureForwardOrRedirectUrl()));
                                   patterns.add(noSeparation.getFailureForwardOrRedirectUrl());
                               }
                               else {
                                   if(StringUtils.hasText(noSeparation.getLoginPageUrl())) {
                                       formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler(noSeparation.getLoginPageUrl()));
                                   }
                                   else {
                                       formLoginConfigurer.failureHandler(new RedirectAuthenticationSuccessOrFailureHandler("/login"));
                                   }
                               }
                           }
                       }
                   }

                   // username / password
                   if(StringUtils.hasText(formLoginProperties.getUsernameParameterName())) {
                       formLoginConfigurer.usernameParameter(formLoginProperties.getUsernameParameterName());
                   }

                   if(StringUtils.hasText(formLoginProperties.getPasswordParameterName())) {
                       formLoginConfigurer.passwordParameter(formLoginProperties.getPasswordParameterName());
                   }

                   if(StringUtils.hasText(formLoginProperties.getLoginProcessUrl())) {
                       formLoginConfigurer.loginProcessingUrl(formLoginProperties.getLoginProcessUrl());
                   }

               }
           };
       }
   }
}
