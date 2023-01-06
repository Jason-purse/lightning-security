package com.generatera.resource.server.config.token;

import com.generatera.resource.server.config.token.entrypoint.LightningAuthenticationEntryPoint;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractLightningTokenAuthenticationFilter extends OncePerRequestFilter implements LightningTokenAuthenticationFilter {

    private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
    private LightningAuthenticationEntryPoint authenticationEntryPoint;
    private LightningAuthenticationTokenResolver tokenResolver;

    private AuthenticationFailureHandler authenticationFailureHandler = (request, response, exception) -> {
        if (exception instanceof AuthenticationServiceException) {
            throw exception;
        } else {
            this.authenticationEntryPoint.commence(request, response, exception);
        }
    };
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

    /**
     * 我们默认是不存储SecurityContext 上下文信息的,子类可以覆盖 ..
     */
    private SecurityContextRepository securityContextRepository = new NullSecurityContextRepository();

    public AbstractLightningTokenAuthenticationFilter(
            AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver, LightningAuthenticationTokenResolver tokenResolver,
            LightningAuthenticationEntryPoint authenticationEntryPoint) {
        Assert.notNull(authenticationManagerResolver, "authenticationManagerResolver cannot be null");
        Assert.notNull(authenticationManagerResolver, "tokenResolver cannot be null");
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
        this.authenticationManagerResolver = authenticationManagerResolver;
        this.tokenResolver = tokenResolver;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public AbstractLightningTokenAuthenticationFilter(AuthenticationManager authenticationManager, LightningAuthenticationTokenResolver tokenResolver,
                                                      LightningAuthenticationEntryPoint authenticationEntryPoint) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.notNull(tokenResolver, "tokenResolver cannot be null");
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
        this.authenticationManagerResolver = (request) -> {
            return authenticationManager;
        };
        this.tokenResolver = tokenResolver;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token;
        try {
            token = this.tokenResolver.resolve(request);
        } catch (AuthenticationException var10) {
            this.logger.trace("Sending to authentication entry point since failed to resolve bearer token", var10);
            this.authenticationEntryPoint.commence(request, response, var10);
            return;
        }

        if (token == null) {
            this.logger.trace("Did not process request since did not find bearer token");
            filterChain.doFilter(request, response);
        } else {

            LightningAuthenticationToken authenticationRequest = generateToken(token);
            authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

            // 认证Token 是否有效 ...
            try {
                AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
                Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authenticationResult);
                SecurityContextHolder.setContext(context);
                this.securityContextRepository.saveContext(context, request, response);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));
                }

                filterChain.doFilter(request, response);
            } catch (AuthenticationException var9) {
                SecurityContextHolder.clearContext();
                this.logger.trace("Failed to process authentication request", var9);
                this.authenticationFailureHandler.onAuthenticationFailure(request, response, var9);
            }

        }
    }

    protected  abstract LightningAuthenticationToken generateToken(String token);

    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
        this.securityContextRepository = securityContextRepository;
    }

    public void setBearerTokenResolver(LightningAuthenticationTokenResolver tokenResolver) {
        Assert.notNull(tokenResolver, "tokenResolver cannot be null");
        this.tokenResolver = tokenResolver;
    }

    public void setAuthenticationEntryPoint(final LightningAuthenticationEntryPoint authenticationEntryPoint) {
        Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    public void setAuthenticationFailureHandler(final AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "authenticationDetailsSource cannot be null");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }
}