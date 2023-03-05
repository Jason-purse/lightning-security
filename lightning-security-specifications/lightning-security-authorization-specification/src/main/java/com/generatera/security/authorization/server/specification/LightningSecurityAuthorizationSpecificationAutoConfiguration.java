package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.annotations.RequestHeaderHandlerMethodArgumentResolver;
import com.generatera.security.authorization.server.specification.components.annotations.UserPrincipalPropertyHandlerMethodArgumentEnhancer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FLJ
 * @date 2023/3/3
 * @time 18:16
 * @Description
 */
@AutoConfiguration
@Configuration
public class LightningSecurityAuthorizationSpecificationAutoConfiguration {
   @Bean
    UserPrincipalPropertyHandlerMethodArgumentEnhancer userPrincipalPropertyHandlerMethodArgumentEnhancer() {
       return new UserPrincipalPropertyHandlerMethodArgumentEnhancer();
   }

   @Bean
    RequestHeaderHandlerMethodArgumentResolver requestHeaderHandlerMethodArgumentResolver() {
       return new RequestHeaderHandlerMethodArgumentResolver();
   }
}
