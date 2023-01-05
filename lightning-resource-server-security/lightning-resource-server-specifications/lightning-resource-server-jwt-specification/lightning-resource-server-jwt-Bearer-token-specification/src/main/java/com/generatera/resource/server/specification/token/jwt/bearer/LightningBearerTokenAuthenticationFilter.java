package com.generatera.resource.server.specification.token.jwt.bearer;

import com.generatera.resource.server.config.token.AbstractLightningTokenAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 14:19
 * @Description 默认的Bearer Token 认证过滤器 ..
 */
public class LightningBearerTokenAuthenticationFilter extends AbstractLightningTokenAuthenticationFilter {
    public LightningBearerTokenAuthenticationFilter(AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) {
        super(authenticationManagerResolver, new DefaultBearerTokenResolver(), new BearerTokenAuthenticationEntryPoint());
    }

    public LightningBearerTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager, new DefaultBearerTokenResolver(), new BearerTokenAuthenticationEntryPoint());
    }

    @Override
    protected BearerAuthenticationToken generateToken(String token) {
        return new BearerAuthenticationToken(token);
    }
}
