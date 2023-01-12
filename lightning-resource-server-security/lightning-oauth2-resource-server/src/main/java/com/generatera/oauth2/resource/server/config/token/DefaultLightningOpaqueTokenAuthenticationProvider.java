package com.generatera.oauth2.resource.server.config.token;

import com.generatera.oauth2.resource.server.config.LightningOAuth2UserPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.util.Assert;

import java.time.Instant;
/**
 * @author FLJ
 * @date 2023/1/12
 * @time 17:00
 * @Description bearer 类型的opaque token 的认证解析提供器
 */
public class DefaultLightningOpaqueTokenAuthenticationProvider implements AuthenticationProvider {

    private final Log logger = LogFactory.getLog(this.getClass());
    private final LightningOAuth2OpaqueTokenIntrospector introspector;

    public DefaultLightningOpaqueTokenAuthenticationProvider(LightningOAuth2OpaqueTokenIntrospector introspector) {
        Assert.notNull(introspector, "introspector cannot be null");
        this.introspector = introspector;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof BearerTokenAuthenticationToken bearer)) {
            return null;
        } else {
            LightningOAuth2UserPrincipal principal = this.getOAuth2AuthenticatedPrincipal(bearer);
            AbstractAuthenticationToken result = this.convert(principal, bearer.getToken());
            result.setDetails(bearer.getDetails());
            this.logger.debug("Authenticated token");
            return result;
        }
    }

    private LightningOAuth2UserPrincipal getOAuth2AuthenticatedPrincipal(BearerTokenAuthenticationToken bearer) {
        try {
            return this.introspector.doIntrospect(bearer.getToken());
        } catch (BadOpaqueTokenException var3) {
            this.logger.debug("Failed to authenticate since token was invalid");
            throw new InvalidBearerTokenException(var3.getMessage(), var3);
        } catch (OAuth2IntrospectionException var4) {
            throw new AuthenticationServiceException(var4.getMessage(), var4);
        }
    }

    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private AbstractAuthenticationToken convert(LightningOAuth2UserPrincipal principal, String token) {
        Instant iat = (Instant)principal.getClaim("iat");
        Instant exp = (Instant)principal.getClaim("exp");
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token, iat, exp);
        return new BearerTokenAuthentication(principal, accessToken, principal.getAuthorities());
    }
}
