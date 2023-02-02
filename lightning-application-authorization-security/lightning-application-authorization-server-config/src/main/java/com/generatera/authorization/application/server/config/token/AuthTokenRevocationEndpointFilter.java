package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.util.AuthHttpResponseUtil;
import com.jianyue.lightning.result.Result;
import com.jianyue.lightning.util.JsonUtil;
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
/**
 * @author FLJ
 * @date 2023/1/31
 * @time 16:00
 * @Description 实现token 撤销 ...
 */
public final class AuthTokenRevocationEndpointFilter extends OncePerRequestFilter {
    private static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI = ProviderSettingProperties.TOKEN_REVOCATION_ENDPOINT;
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher tokenRevocationEndpointMatcher;
    private AuthenticationConverter authenticationConverter;
    private final HttpMessageConverter<LightningAuthError> errorHttpResponseConverter;
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private AuthenticationFailureHandler authenticationFailureHandler;

    public AuthTokenRevocationEndpointFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI);
    }

    public AuthTokenRevocationEndpointFilter(AuthenticationManager authenticationManager, String tokenRevocationEndpointUri) {
        this.authenticationConverter = new AuthTokenRevocationEndpointFilter.DefaultTokenRevocationAuthenticationConverter();
        this.errorHttpResponseConverter = new AuthErrorHttpMessageConverter();
        this.authenticationSuccessHandler = this::sendRevocationSuccessResponse;
        this.authenticationFailureHandler = this::sendErrorResponse;
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.hasText(tokenRevocationEndpointUri, "tokenRevocationEndpointUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.tokenRevocationEndpointMatcher = new AntPathRequestMatcher(tokenRevocationEndpointUri, HttpMethod.POST.name());
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.tokenRevocationEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                Authentication tokenRevocationAuthentication = this.authenticationConverter.convert(request);
                Authentication tokenRevocationAuthenticationResult = this.authenticationManager.authenticate(tokenRevocationAuthentication);
                this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, tokenRevocationAuthenticationResult);
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

    private void sendRevocationSuccessResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setStatus(HttpStatus.OK.value());
        AuthHttpResponseUtil.commence(response, JsonUtil.getDefaultJsonUtil().asJSON(
                Result.success(HttpStatus.OK.value(),"REVOKE TOKEN SUCCESS")
        ));
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        LightningAuthError error = ((LightningAuthenticationException)exception).getError();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.OK);

        this.errorHttpResponseConverter.write(error, null, httpResponse);
    }

    private static void throwError(String errorCode, String parameterName) {
        LightningAuthError error = new LightningAuthError(errorCode, "Auth Token Revocation Parameter: " + parameterName, "https://datatracker.ietf.org/doc/html/rfc7009#section-2.1");
        throw new LightningAuthenticationException(error);
    }

    private static class DefaultTokenRevocationAuthenticationConverter implements AuthenticationConverter {
        private DefaultTokenRevocationAuthenticationConverter() {
        }

        public Authentication convert(HttpServletRequest request) {
            MultiValueMap<String, String> parameters = HttpRequestUtil.getParameters(request);
            String token = parameters.getFirst("token");
            if (!StringUtils.hasText(token) || parameters.get("token").size() != 1) {
                AuthTokenRevocationEndpointFilter.throwError("invalid_request", "token");
            }

            String tokenTypeHint = parameters.getFirst("token_type_hint");
            if (StringUtils.hasText(tokenTypeHint) && parameters.get("token_type_hint").size() != 1) {
                AuthTokenRevocationEndpointFilter.throwError("invalid_request", "token_type_hint");
            }

            return new AuthTokenRevocationAuthenticationToken(token,null, tokenTypeHint);
        }
    }
}