package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import com.generatera.resource.server.config.token.LightningTokenAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 代理 BearerToken 解析工作到 oauth2 BearerToken 认证过滤器上 ...
 */
public class OAuth2BearerTokenAuthenticationFilter extends OncePerRequestFilter implements LightningTokenAuthenticationFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        OAuth2BearerTokenAuthenticationFilterProvider.getFilter().doFilter(request,response,filterChain);
    }
}
