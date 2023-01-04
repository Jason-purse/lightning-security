package com.generatera.authorization.application.server.config.specification;

import com.generatera.authorization.server.common.configuration.ext.oauth2.provider.ProviderContextHolder;
import com.generatera.authorization.server.common.configuration.ext.oauth2.provider.ProviderSettings;
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
import java.util.List;
import java.util.function.Consumer;
/**
 * @author FLJ
 * @date 2023/1/4
 * @time 10:45
 * @Description 让form-login 也遵循 oauth2 的部分规范
 * 1. token 撤销
 * 2. token 自省
 * 3. 基于授权中心获取 jwk set 进一步配置自己
 */
public final class BasedOAuth2AuthorizationServerForSpecificationMetadataEndpointFilter extends OncePerRequestFilter {
    private static final String DEFAULT_AUTH_AUTHORIZATION_SERVER_METADATA_ENDPOINT_URI = "/.well-known/oauth-authorization-server";
    private final ProviderSettings providerSettings;
    private final RequestMatcher requestMatcher;
    private final OAuth2AuthorizationServerMetadataHttpMessageConverter authorizationServerMetadataHttpMessageConverter = new OAuth2AuthorizationServerMetadataHttpMessageConverter();

    public BasedOAuth2AuthorizationServerForSpecificationMetadataEndpointFilter(ProviderSettings providerSettings) {
        Assert.notNull(providerSettings, "providerSettings cannot be null");
        this.providerSettings = providerSettings;
        this.requestMatcher = new AntPathRequestMatcher("/.well-known/oauth-authorization-server", HttpMethod.GET.name());
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            String issuer = ProviderContextHolder.getProviderContext().getIssuer();
            OAuth2AuthorizationServerMetadata authorizationServerMetadata =
                    OAuth2AuthorizationServerMetadata.builder()
                            .issuer(issuer)
                            .authorizationEndpoint(asUrl(issuer, this.providerSettings.getAuthorizationEndpoint()))
            .jwkSetUrl(asUrl(issuer, this.providerSettings.getJwkSetEndpoint()))
            .tokenRevocationEndpoint(asUrl(issuer, this.providerSettings.getTokenRevocationEndpoint()))
            .tokenRevocationEndpointAuthenticationMethods(clientAuthenticationMethods())
            .tokenIntrospectionEndpoint(asUrl(issuer, this.providerSettings.getTokenIntrospectionEndpoint()))
            .tokenIntrospectionEndpointAuthenticationMethods(clientAuthenticationMethods())
            .codeChallengeMethod("S256")
            .build();
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            this.authorizationServerMetadataHttpMessageConverter.write(authorizationServerMetadata, MediaType.APPLICATION_JSON, httpResponse);
        }
    }

    // todo 没有客户端
    private static Consumer<List<String>> clientAuthenticationMethods() {
        return (authenticationMethods) -> {
            //authenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
            //authenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue());
            //authenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue());
            //authenticationMethods.add(ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue());
        };
    }

    private static String asUrl(String issuer, String endpoint) {
        return UriComponentsBuilder.fromUriString(issuer).path(endpoint).toUriString();
    }
}