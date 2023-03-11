package com.generata.lightning.application.authorization.oauth2.login.client.server.test;

import com.generatera.authorization.application.server.config.LoginGrantType;
import com.generatera.authorization.application.server.config.token.AuthParameterNames;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.OAuth2loginParameterNames;
import com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support.OAuth2AuthorizationRequestAndExtRedirectFilter;
import com.generatera.authorization.application.server.oauth2.login.config.client.register.DelegateClientRegistrationRepository;
import com.generatera.authorization.application.server.oauth2.login.config.client.register.LightningOAuth2ClientRegistrationRepository;
import com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 授权请求扩展过滤器单元测试
 */
@SpringJUnitWebConfig
public class AuthorizationRequestExtFilterTests {

    @Configuration
    public static class MyConfig {

        @Bean
        LightningOAuth2ClientRegistrationRepository clientRegistrationRepository() {
            return new DelegateClientRegistrationRepository(
           new InMemoryClientRegistrationRepository(
                    ClientRegistration
                            .withRegistrationId("default")
                            .clientId("usercenter-authorization-proxy")
                            .clientSecret("secret3")
                            .clientName("用户认证代理BFF")
                            .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .tokenUri("http://127.0.0.1:9000/oauth2/token")
                            .jwkSetUri("http://127.0.0.1:9000/oauth2/jwks")
                            .issuerUri("http://127.0.0.1:9000")
                            .authorizationUri("http://127.0.0.1:9000/auth2/token")
                            .redirectUri("http://127.0.0.1:8080/login/oauth2/code")
                            .scope("openid")
                            .build()
            )
            );
        }
    }

    @Autowired
    private LightningOAuth2ClientRegistrationRepository clientRegistrationRepository;
    @Test
    public void test(WebApplicationContext applicationContext) throws Exception {
        MockMvc build = MockMvcBuilders.webAppContextSetup(applicationContext).addFilter(
                new OAuth2AuthorizationRequestAndExtRedirectFilter(clientRegistrationRepository)
        ).build();


        build.perform(
                MockMvcRequestBuilders.get(OAuth2AuthorizationRequestAndExtRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_EXT_BASE_URL+"/" + "default")
                        .param("username","admin")
                        .param("password","123456")
                        .param(AuthParameterNames.LOGIN_GRANT_TYPE, LoginGrantType.OAUTH2_CLIENT_LOGIN.value())
                        .param(AuthParameterNames.GRANT_TYPE, LightningAuthorizationGrantType.ACCESS_TOKEN.getValue())
                        .param(OAuth2loginParameterNames.OAUTH2_GRANT_TYPE, AuthorizationGrantType.PASSWORD.getValue())
        )
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("http://127.0.0.1:9000/**"));


    }
}
