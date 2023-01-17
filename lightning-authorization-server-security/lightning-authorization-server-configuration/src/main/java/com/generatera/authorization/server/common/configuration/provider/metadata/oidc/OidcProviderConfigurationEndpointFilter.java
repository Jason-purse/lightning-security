package com.generatera.authorization.server.common.configuration.provider.metadata.oidc;

import com.generatera.security.authorization.server.specification.components.token.SignatureAlgorithm;
import com.generatera.security.authorization.server.specification.components.provider.ProviderContextHolder;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class OidcProviderConfigurationEndpointFilter extends OncePerRequestFilter {
    public static final String DEFAULT_OIDC_PROVIDER_CONFIGURATION_ENDPOINT_URI = "/.well-known/openid-configuration";
    private final ProviderSettings providerSettings;
    private final RequestMatcher requestMatcher;
    private final OidcProviderConfigurationHttpMessageConverter providerConfigurationHttpMessageConverter = new OidcProviderConfigurationHttpMessageConverter();

    public OidcProviderConfigurationEndpointFilter(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
        this.providerSettings = providerSettings;
        this.requestMatcher = new AntPathRequestMatcher("/.well-known/openid-configuration", HttpMethod.GET.name());
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            String issuer = ProviderContextHolder.getProviderContext().getIssuer();
            OidcProviderConfiguration providerConfiguration = OidcProviderConfiguration.builder()
                    .issuer(issuer)
                    .jwkSetUrl(asUrl(issuer, this.providerSettings.getJwkSetEndpoint()))
                    //.responseType(AuthorizationGrantType.ACCESS_TOKEN.getValue())
                    //.grantType(AuthorizationGrantType.AUTHORIZATION_CODE.getValue())
                    //.grantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue())
                    //.grantType(AuthorizationGrantType.REFRESH_TOKEN.getValue())
                    .subjectType("public")
                    .idTokenSigningAlgorithm(SignatureAlgorithm.RS256.getName())
                    .scope("openid")
                    .build();
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            this.providerConfigurationHttpMessageConverter.write(providerConfiguration, MediaType.APPLICATION_JSON, httpResponse);
        }
    }


    private static String asUrl(String issuer, String endpoint) {
        return UriComponentsBuilder.fromUriString(issuer).path(endpoint).build().toUriString();
    }
}
