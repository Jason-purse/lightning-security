package com.generatera.app.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author FLJ
 * @date 2022/12/27
 * @time 11:18
 * @Description 资源服务器配置
 */
@Configuration
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain httpSecurity(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.authorizeHttpRequests()
                .antMatchers("/api/*")
                .permitAll()
                .and()
                .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .password()
                        .refreshToken()
                        .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        // Assuming the `username` and `password` are supplied as `HttpServletRequest` parameters,
        // map the `HttpServletRequest` parameters to `OAuth2AuthorizationContext.getAttributes()`
        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper());

        return authorizedClientManager;
    }

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> contextAttributesMapper() {
        return authorizeRequest -> {
            Map<String, Object> contextAttributes = Collections.emptyMap();
            HttpServletRequest servletRequest = authorizeRequest.getAttribute(HttpServletRequest.class.getName());
            String username = servletRequest.getParameter(OAuth2ParameterNames.USERNAME);
            String password = servletRequest.getParameter(OAuth2ParameterNames.PASSWORD);
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                contextAttributes = new HashMap<>();

                // `PasswordOAuth2AuthorizedClientProvider` requires both attributes
                contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);
                contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);
            }
            return contextAttributes;
        };
    }

}
