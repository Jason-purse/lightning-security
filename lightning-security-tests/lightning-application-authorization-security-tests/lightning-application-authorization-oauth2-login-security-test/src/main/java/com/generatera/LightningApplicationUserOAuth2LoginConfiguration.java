package com.generatera;

import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * @author Sun.
 */
@Configuration
public class LightningApplicationUserOAuth2LoginConfiguration {


    @Bean
    public LightningOidcUserService oidcUserService(UserDetailsService userDetailsService) {


        return new LightningOidcUserService() {
            private final OidcUserService oidcUserService = new OidcUserService();

            @Override
            public OidcUser loadUser(OidcUserRequest userRequest)  throws OAuth2AuthenticationException {
                OidcUser oidcUser = oidcUserService.loadUser(userRequest);
                Object openId = oidcUser.getAttributes().get("openid");
                UserDetails userDetails = userDetailsService.loadUserByUsername(openId.toString());
                return new LightningUserDetails(userDetails,oidcUser);
            }
        };
    }
}
