package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 12:13
 * @Description 进行Token 省查 ..
 *
 * 针对 opaque token 进行 token 省查 ..
 */
public class AuthTokenIntrospectEndpointFilter extends OncePerRequestFilter {
    private static final String DEFAULT_TOKEN_INTROSPECTION_ENDPOINT_URI = ProviderSettingProperties.TOKEN_INTROSPECTION_ENDPOINT;
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher tokenIntrospectionEndpointMatcher;
    private AuthenticationConverter authenticationConverter;
    private final HttpMessageConverter<AuthTokenIntrospection> tokenIntrospectionHttpResponseConverter;
    private final HttpMessageConverter<LightningAuthError> errorHttpResponseConverter;
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private AuthenticationFailureHandler authenticationFailureHandler;

    public AuthTokenIntrospectEndpointFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, DEFAULT_TOKEN_INTROSPECTION_ENDPOINT_URI);
    }

    public AuthTokenIntrospectEndpointFilter(AuthenticationManager authenticationManager, String tokenIntrospectionEndpointUri) {
        this.authenticationConverter = new DefaultTokenIntrospectionAuthenticationConverter();
        this.tokenIntrospectionHttpResponseConverter = new AuthTokenIntrospectionHttpMessageConverter();
        this.errorHttpResponseConverter = new AuthErrorHttpMessageConverter();
        this.authenticationSuccessHandler = this::sendIntrospectionResponse;
        this.authenticationFailureHandler = this::sendErrorResponse;
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.hasText(tokenIntrospectionEndpointUri, "tokenIntrospectionEndpointUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.tokenIntrospectionEndpointMatcher = new AntPathRequestMatcher(tokenIntrospectionEndpointUri, HttpMethod.POST.name());
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.tokenIntrospectionEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                Authentication tokenIntrospectionAuthentication = this.authenticationConverter.convert(request);
                Authentication tokenIntrospectionAuthenticationResult = this.authenticationManager.authenticate(tokenIntrospectionAuthentication);
                // 成功响应即可 ..
                this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, tokenIntrospectionAuthenticationResult);
            } catch (LightningAuthenticationException var6) {
                SecurityContextHolder.clearContext();
                this.authenticationFailureHandler.onAuthenticationFailure(request, response, var6);
            }

        }
    }

    public void setAuthenticationConverter(AuthenticationConverter authenticationConverter) {
        Assert.notNull(authenticationConverter, "authenticationConverter cannot be null");
        this.authenticationConverter = authenticationConverter;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    private void sendIntrospectionResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        AuthTokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication = (AuthTokenIntrospectionAuthenticationToken)authentication;
        AuthTokenIntrospection tokenClaims = tokenIntrospectionAuthentication.getTokenClaims();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.tokenIntrospectionHttpResponseConverter.write(tokenClaims, null, httpResponse);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        LightningAuthError error = ((LightningAuthenticationException)exception).getError();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        this.errorHttpResponseConverter.write(error, null, httpResponse);
    }

    private static void throwError(String errorCode, String parameterName) {
        LightningAuthError error = new LightningAuthError(errorCode, "Token Introspection Parameter: " + parameterName, "https://datatracker.ietf.org/doc/html/rfc7662#section-2.1");
        throw new LightningAuthenticationException(error);
    }

    private static class DefaultTokenIntrospectionAuthenticationConverter implements AuthenticationConverter {
        private DefaultTokenIntrospectionAuthenticationConverter() {
        }

        public Authentication convert(HttpServletRequest request) {
            Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
            MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
            String token = parameters.getFirst("token");
            if (!StringUtils.hasText(token) || parameters.get("token").size() != 1) {
                throwError("invalid_request", "token");
            }

            String tokenTypeHint = parameters.getFirst("token_type_hint");
            if (StringUtils.hasText(tokenTypeHint) && parameters.get("token_type_hint").size() != 1) {
                throwError("invalid_request", "token_type_hint");
            }

            Map<String, Object> additionalParameters = new HashMap<>();
            parameters.forEach((key, value) -> {
                if (!key.equals("token") && !key.equals("token_type_hint")) {
                    additionalParameters.put(key, value.get(0));
                }

            });
            return new AuthTokenIntrospectionAuthenticationToken(token, clientPrincipal, tokenTypeHint, additionalParameters);
        }
    }
}
