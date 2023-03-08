package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthError;
import com.generatera.security.authorization.server.specification.components.authorization.LightningAuthenticationException;
import com.generatera.security.authorization.server.specification.components.provider.ProviderSettingProperties;
import com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/1/28
 * @time 16:35
 * @Description 进行 token 端点拦截,实现 刷新token获取新token ....
 * <p>
 * 后续修改,普通认证服务器使用token 端点进行token派发 ...
 */
public final class AuthTokenEndpointFilter extends OncePerRequestFilter {
    private static final String DEFAULT_TOKEN_ENDPOINT_URI = ProviderSettingProperties.TOKEN_ENDPOINT;
    private static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher tokenEndpointMatcher;
    private final HttpMessageConverter<ApplicationLevelAuthorizationToken> accessTokenHttpResponseConverter;
    private final HttpMessageConverter<LightningAuthError> errorHttpResponseConverter;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    private AuthenticationConverter authenticationConverter;
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private AuthenticationFailureHandler authenticationFailureHandler;

    public AuthTokenEndpointFilter(AuthenticationManager authenticationManager) {
        this(authenticationManager, DEFAULT_TOKEN_ENDPOINT_URI);
    }

    public AuthTokenEndpointFilter(AuthenticationManager authenticationManager, String tokenEndpointUri) {
        this.accessTokenHttpResponseConverter = new ApplicationLevelAuthorizationTokenHttpMessageConverter();
        this.errorHttpResponseConverter = new AuthErrorHttpMessageConverter();
        this.authenticationDetailsSource = new WebAuthenticationDetailsSource();
        this.authenticationSuccessHandler = this::sendAccessTokenResponse;
        this.authenticationFailureHandler = this::sendErrorResponse;
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        Assert.hasText(tokenEndpointUri, "tokenEndpointUri cannot be empty");
        this.authenticationManager = authenticationManager;
        this.tokenEndpointMatcher = new AntPathRequestMatcher(tokenEndpointUri, HttpMethod.POST.name());
        this.authenticationConverter = new DelegatingAuthenticationConverter(
                List.of(
                        new AuthLoginRequestAuthenticationConverter(),
                        new AuthRefreshTokenAuthenticationConverter()));
    }

    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.tokenEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                String[] grantTypes = request.getParameterValues("grant_type");
                if (grantTypes == null || grantTypes.length != 1) {
                    throwError("invalid_request", "grant_type");
                }

                Authentication authorizationGrantAuthentication = this.authenticationConverter.convert(request);
                if (authorizationGrantAuthentication == null) {
                    throwError("unsupported_grant_type", "grant_type");
                }

                if (authorizationGrantAuthentication instanceof AbstractAuthenticationToken) {
                    ((AbstractAuthenticationToken) authorizationGrantAuthentication).setDetails(this.authenticationDetailsSource.buildDetails(request));
                }

                // 处理重定向 ....
                // oauth2 grant login
                if (authorizationGrantAuthentication instanceof AuthorizationRequestAuthentication requestAuthentication) {
                    // 直接登录的,不需要重定向 !!!
                    if(requestAuthentication.needRedirect()) {
                        try {
                            requestAuthentication.sendRedirect(request,response);
                        }catch (Exception e) {
                            // pass
                            // 不做任何提示 ...
                            throw new LightningAuthenticationException(new LightningAuthError("invalid_redirect_uri"));
                        }
                    }
                }
                else {
                    AuthAccessTokenAuthenticationToken accessTokenAuthentication = (AuthAccessTokenAuthenticationToken) this.authenticationManager.authenticate(authorizationGrantAuthentication);
                    this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, accessTokenAuthentication);
                }
             } catch (AuthenticationException  var7) { // 只要是认证异常 都接收 ...
                SecurityContextHolder.clearContext();
                this.authenticationFailureHandler.onAuthenticationFailure(request, response, var7);
            }

        }
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "authenticationDetailsSource cannot be null");
        this.authenticationDetailsSource = authenticationDetailsSource;
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

    private void sendAccessTokenResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        AuthAccessTokenAuthenticationToken accessTokenAuthentication = (AuthAccessTokenAuthenticationToken) authentication;
        LightningAccessToken accessToken = accessTokenAuthentication.getAccessToken();
        var refreshToken = accessTokenAuthentication.getRefreshToken();
        Map<String, Object> additionalParameters = accessTokenAuthentication.getAdditionalParameters();
        ApplicationLevelAuthorizationToken authorizationToken = ApplicationLevelAuthorizationToken.of(
                accessToken,
                refreshToken
        );
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.accessTokenHttpResponseConverter.write(authorizationToken, null, httpResponse);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(HttpStatus.BAD_REQUEST);
        if (exception instanceof LightningAuthenticationException lightningAuthenticationException) {
            LightningAuthError error = lightningAuthenticationException.getError();
            this.errorHttpResponseConverter.write(error, null, httpResponse);
        } else {
            if (exception.getCause() != null) {
                if (exception.getCause() instanceof AuthenticationException value) {
                    sendErrorResponse(request, response, value);
                }
            } else {
                // 否则就是普通异常..
                this.errorHttpResponseConverter.write(
                        new LightningAuthError(
                                "invalid_request",
                                exception.getMessage(), ""
                        ),
                        null, httpResponse
                );
            }
        }

    }

    private static void throwError(String errorCode, String parameterName) {
        LightningAuthError error = new LightningAuthError(errorCode, "Auth Parameter: " + parameterName, "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2");
        throw new LightningAuthenticationException(error);
    }
}