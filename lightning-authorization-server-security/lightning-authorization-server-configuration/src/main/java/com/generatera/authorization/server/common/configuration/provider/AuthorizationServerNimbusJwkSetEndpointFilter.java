package com.generatera.authorization.server.common.configuration.provider;

import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * @author FLJ
 * @date 2023/1/4
 * @time 10:48
 * @Description 基于 oauth2  jwk set 规范
 */
public final class AuthorizationServerNimbusJwkSetEndpointFilter extends OncePerRequestFilter {
    private static final String DEFAULT_JWK_SET_ENDPOINT_URI = ProviderSettingProperties.JWT_SET_ENDPOINT;
    private final JWKSource<SecurityContext> jwkSource;
    private final JWKSelector jwkSelector;
    private final RequestMatcher requestMatcher;

    public AuthorizationServerNimbusJwkSetEndpointFilter(JWKSource<SecurityContext> jwkSource) {
        this(jwkSource, DEFAULT_JWK_SET_ENDPOINT_URI);
    }

    public AuthorizationServerNimbusJwkSetEndpointFilter(JWKSource<SecurityContext> jwkSource, String jwkSetEndpointUri) {
        Assert.notNull(jwkSource, "jwkSource cannot be null");
        Assert.hasText(jwkSetEndpointUri, "jwkSetEndpointUri cannot be empty");
        this.jwkSource = jwkSource;
        this.jwkSelector = new JWKSelector((new JWKMatcher.Builder()).build());
        this.requestMatcher = new AntPathRequestMatcher(jwkSetEndpointUri, HttpMethod.GET.name());
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            JWKSet jwkSet;
            try {
                jwkSet = new JWKSet(this.jwkSource.get(this.jwkSelector, (SecurityContext)null));
            } catch (Exception var9) {
                throw new IllegalStateException("Failed to select the JWK(s) -> " + var9.getMessage(), var9);
            }

            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();

            try {
                writer.write(jwkSet.toString());
            } catch (Throwable var10) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable var8) {
                        var10.addSuppressed(var8);
                    }
                }

                throw var10;
            }

            if (writer != null) {
                writer.close();
            }

        }
    }
}