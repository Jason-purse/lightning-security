package com.generatera.central.oauth2.authorization.server.configuration.components.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/2/3
 * @time 15:00
 * @Description 提前感知 user 上下文, 释放 表单登录的 success url 参数 ...
 */
public class OptimizedForOAuth2AuthorizationEndpointFilter extends OncePerRequestFilter {
    public static final String DEFAULT_AUTHORIZATION_CODE_REQUEST_URL_ATTRIBUTE = "AUTHORIZATION_CODE_REQUEST_FLOW_REDIRECT_URL";

    private String authorizationCodeRequestUrlAttribute = DEFAULT_AUTHORIZATION_CODE_REQUEST_URL_ATTRIBUTE;

    private RequestMatcher oauthorizationCodeEndpointMatcher = new AntPathRequestMatcher("/oauth2/authorize");

    /**
     * 默认开启 session url 重写 ...
     *
     * 要保证,感知 authorization code flow的流程(必须是相同session,否则设置的session 也将无效) ...
     */
    private final DefaultRedirectStrategy defaultRedirectStrategy = new DefaultRedirectStrategy();

    private String loginPageUrl = "/login";

    public void setAuthorizationCodeRequestUrlAttribute(String authorizationCodeRequestUrlAttribute) {
        Assert.hasText(authorizationCodeRequestUrlAttribute, "authorizationCodeRequestUrlAttribute must not be null !!!");
        this.authorizationCodeRequestUrlAttribute = authorizationCodeRequestUrlAttribute;
    }

    public void setLoginPageUrl(String loginPageUrl) {
        Assert.notNull(loginPageUrl, "loginPageUrl must not be null !!!");
        this.loginPageUrl = loginPageUrl;
    }

    public void setOauthorizationCodeEndpointMatcher(RequestMatcher oauthorizationCodeEndpointMatcher) {
        Assert.notNull(oauthorizationCodeEndpointMatcher, "oauthorizationCodeEndpointMatcher must not be null !!!");
        this.oauthorizationCodeEndpointMatcher = oauthorizationCodeEndpointMatcher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (oauthorizationCodeEndpointMatcher.matches(request)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                // 开启一个session
                HttpSession session = request.getSession(true);
                session.setAttribute(authorizationCodeRequestUrlAttribute, parseUrl(request));

                // 发起重定向 ...
                defaultRedirectStrategy.sendRedirect(request, response, loginPageUrl);
                // 直接返回 ...
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String parseUrl(HttpServletRequest request) {
        return UriComponentsBuilder.fromUriString(request.getRequestURI())
                .queryParams(getParameters(request))
                .build().toUriString();
    }

    public static MultiValueMap<String, String> getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            if (values.length > 0) {
                for (String value : values) {
                    parameters.add(key, value);
                }
            }
        });
        return parameters;
    }
}
