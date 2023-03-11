package com.generatera.authorization.application.server.oauth2.login.config.authorization.grant.support;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 我们同样采用 过滤器重定向,行为类似  {@link  org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter}
 * 最后将响应 .. 重定向到{@link  org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter}上进行处理  ..
 *
 */
public class OAuth2AuthorizationRequestAndExtRedirectFilter extends OncePerRequestFilter {
    /**
     * 扩展支持 ...
     */
    public static final String DEFAULT_AUTHORIZATION_REQUEST_EXT_BASE_URL = "/oauth2/authorization/ext";
    /**
     * 进行 解析 转换为 OAuth2AuthorizationExtRequest
     */
    private LightningOAuth2AuthorizationExtRequestResolver auth2AuthorizationExtRequestResolver;

    private final RedirectStrategy authorizationRedirectStrategy;

    private final ThrowableAnalyzer throwableAnalyzer;

    public OAuth2AuthorizationRequestAndExtRedirectFilter(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationExtRequestBaseUri
    ) {
        this.authorizationRedirectStrategy = new DefaultRedirectStrategy();
        this.throwableAnalyzer = new DefaultThrowableAnalyzer();
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.hasText(authorizationExtRequestBaseUri, "authorizationRequestBaseUri cannot be empty");
        this.auth2AuthorizationExtRequestResolver = new DefaultOauth2AuthorizationExtRequestResolver(
                clientRegistrationRepository,
                authorizationExtRequestBaseUri
        );

    }

    public OAuth2AuthorizationRequestAndExtRedirectFilter(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        this(clientRegistrationRepository,DEFAULT_AUTHORIZATION_REQUEST_EXT_BASE_URL);
    }


    public void setAuth2AuthorizationExtRequestResolver(LightningOAuth2AuthorizationExtRequestResolver requestResolver) {
        this.auth2AuthorizationExtRequestResolver = requestResolver;
    }


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            OAuth2AuthorizationExtRequest authorizationRequest = this.auth2AuthorizationExtRequestResolver.resolve(request);
            if(authorizationRequest != null) {
                this.sendRedirectForAuthorization(request, response, authorizationRequest);
                return;
            }
        } catch (Exception var11) {
            this.unsuccessfulRedirectForAuthorization(request, response, var11);
            return;
        }

        try {
            // 交给默认的,也需要判断异常 ..
            filterChain.doFilter(request,response);
        } catch (IOException var9) {
            throw var9;
        } catch (Exception var10) {
            Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(var10);
            ClientAuthorizationRequiredException authzEx = (ClientAuthorizationRequiredException)this.throwableAnalyzer.getFirstThrowableOfType(ClientAuthorizationRequiredException.class, causeChain);
            if (authzEx != null) {
                this.unsuccessfulRedirectForAuthorization(request,response,authzEx);
            } else if (var10 instanceof ServletException) {
                throw (ServletException)var10;
            } else if (var10 instanceof RuntimeException) {
                throw (RuntimeException)var10;
            } else {
                throw new RuntimeException(var10);
            }
        }
    }

    private void sendRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response, OAuth2AuthorizationExtRequest authorizationRequest) throws IOException {
        this.authorizationRedirectStrategy.sendRedirect(request, response, authorizationRequest.getAuthorizationRequestUri());
    }

    private void unsuccessfulRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        this.logger.error(LogMessage.format("Authorization Request failed: %s", ex), ex);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {
        private DefaultThrowableAnalyzer() {
        }

        protected void initExtractorMap() {
            super.initExtractorMap();
            this.registerExtractor(ServletException.class, (throwable) -> {
                ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
                return ((ServletException)throwable).getRootCause();
            });
        }
    }
}
