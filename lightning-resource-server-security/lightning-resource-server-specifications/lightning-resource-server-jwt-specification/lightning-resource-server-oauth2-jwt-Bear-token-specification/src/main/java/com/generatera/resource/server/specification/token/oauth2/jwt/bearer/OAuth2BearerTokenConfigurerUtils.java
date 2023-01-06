package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import com.generatera.resource.server.config.token.entrypoint.DefaultForbiddenAuthenticationEntryPoint;
import com.generatera.resource.server.config.token.entrypoint.LightningAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.Map;

/**
 * 用来从bean 容器中获取
 */
@Slf4j
public class OAuth2BearerTokenConfigurerUtils {

    public static OAuth2BearerTokenAuthenticationEntryPoint getAuthenticationEntryPoint(HttpSecurity httpSecurity) {

        LightningAuthenticationEntryPoint sharedObject = httpSecurity.getSharedObject(LightningAuthenticationEntryPoint.class);

        if(sharedObject == null) {
            ApplicationContext applicationContext = httpSecurity.getSharedObject(ApplicationContext.class);
            Map<String,LightningAuthenticationEntryPoint> beans = applicationContext.getBeansOfType(LightningAuthenticationEntryPoint.class);
            if(beans.size() > 0) {
                if(beans.size() > 1) {
                    throw new IllegalArgumentException("lightningAuthenticationEntryPoint just one is need !!!");
                }
                sharedObject = beans.values().iterator().next();
            }
            else {
                sharedObject = new DefaultForbiddenAuthenticationEntryPoint();
            }
        }
        log.info("################################################################################################");
        log.info("oauth2 resource server register one default defaultOAuth2BearerTokenAuthenticationEntryPoint !!!");
        log.info("################################################################################################");
        return new DefaultOAuth2BearerTokenAuthenticationEntryPoint(sharedObject);
    }
}
