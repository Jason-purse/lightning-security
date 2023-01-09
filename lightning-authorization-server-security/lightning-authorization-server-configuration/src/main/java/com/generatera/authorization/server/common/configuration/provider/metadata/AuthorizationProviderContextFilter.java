package com.generatera.authorization.server.common.configuration.provider.metadata;

import com.generatera.security.authorization.server.specification.components.provider.ProviderContext;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContextHolder;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @author FLJ
 * @date 2023/1/4
 * @time 10:19
 * @Description oauth2 server copy
 */
public final class AuthorizationProviderContextFilter extends OncePerRequestFilter {
    private final ProviderSettings providerSettings;

    public AuthorizationProviderContextFilter(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
        this.providerSettings = providerSettings;
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            ProviderContext providerContext = new ProviderContext(this.providerSettings,
                    () -> resolveIssuer(this.providerSettings, request));
            ProviderContextHolder.setProviderContext(providerContext);
            filterChain.doFilter(request, response);
        } finally {
            ProviderContextHolder.resetProviderContext();
        }

    }

    private static String resolveIssuer(ProviderSettings providerSettings, HttpServletRequest request) {
        return providerSettings.getIssuer() != null ? providerSettings.getIssuer() : getContextPath(request);
    }

    private static String getContextPath(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request)).replacePath(request.getContextPath()).replaceQuery((String)null).fragment((String)null).build().toUriString();
    }
}