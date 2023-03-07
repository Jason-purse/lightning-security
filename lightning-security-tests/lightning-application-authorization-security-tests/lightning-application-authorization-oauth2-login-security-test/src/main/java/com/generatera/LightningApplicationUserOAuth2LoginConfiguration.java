package com.generatera;

import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.DefaultLightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOAuth2UserService;
import com.generatera.authorization.application.server.oauth2.login.config.authority.LightningOidcUserService;
import com.generatera.authorization.application.server.oauth2.login.config.user.LightningOAuth2UserDetails;
import com.generatera.authorization.application.server.oauth2.login.config.user.OidcUserDetails;
import com.generatera.security.authorization.server.specification.DefaultLightningUserDetails;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import com.generatera.security.authorization.server.specification.components.token.JwtClaimsToUserPrincipalMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
//                OidcUserInfo userInfo = oidcUser.getUserInfo();
//                OidcUserInfo.Builder claim = OidcUserInfo.builder().claim("authorities", authorities);
//                DefaultOidcUser defaultOidcUser = new DefaultOidcUser( authorities, oidcUser.getIdToken(), userInfo);
//                JwtClaimsToUserPrincipalMapper claims = new JwtClaimsToUserPrincipalMapper() {
//                    @Override
//                    public LightningUserPrincipal convert(Map<String, Object> claims) {
////                        claims.put("authorities", Lists.newArrayList("cc","dd"));
//                        OidcUserDetails userDetails = new OidcUserDetails(oidcUser);
//                        claims.put("authorities", Lists.newArrayList("cc", "dd"));
//                        return new DefaultLightningUserDetails(userDetails);
//                    }
//                };
//                Map<String, Object> claimMap = oidcUser.getIdToken().getClaims();


                UserDetails userDetails = userDetailsService.loadUserByUsername(openId.toString());

                LightningUserDetails lightningUserDetails = new LightningUserDetails(userDetails,oidcUser);

              return lightningUserDetails;
            }
        };
    }
}
