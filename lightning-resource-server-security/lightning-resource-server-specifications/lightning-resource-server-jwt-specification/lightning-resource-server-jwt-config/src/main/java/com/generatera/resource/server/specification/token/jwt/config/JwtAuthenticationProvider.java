package com.generatera.resource.server.specification.token.jwt.config;

import com.generatera.resource.server.specification.token.jwt.config.exception.BadJwtException;
import com.generatera.resource.server.specification.token.jwt.config.exception.InvalidJwtTokenException;
import com.generatera.resource.server.specification.token.jwt.config.exception.JwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 15:37
 * @Description Jwt 认证Provider ...
 */
public final class JwtAuthenticationProvider implements AuthenticationProvider {
    private final Log logger = LogFactory.getLog(this.getClass());
    private final LightningJwtDecoder jwtDecoder;
    private Converter<LightningJwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();

    public JwtAuthenticationProvider(LightningJwtDecoder jwtDecoder) {
        Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
        this.jwtDecoder = jwtDecoder;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken bearer = (JwtAuthenticationToken)authentication;
        LightningJwt jwt = this.getJwt(bearer);
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        token.setDetails(bearer.getDetails());
        this.logger.debug("Authenticated token");
        return token;
    }


    private LightningJwt getJwt(JwtAuthenticationToken jwtTokenAuthenticationToken) {
        try {
            return this.jwtDecoder.decode(jwtTokenAuthenticationToken.getToken());
        } catch (BadJwtException var3) {
            this.logger.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidJwtTokenException(var3.getMessage(), var3);
        } catch (JwtException var4) {
            throw new AuthenticationServiceException(var4.getMessage(), var4);
        }
    }

    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setJwtAuthenticationConverter(Converter<LightningJwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) {
        Assert.notNull(jwtAuthenticationConverter, "jwtAuthenticationConverter cannot be null");
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }
}