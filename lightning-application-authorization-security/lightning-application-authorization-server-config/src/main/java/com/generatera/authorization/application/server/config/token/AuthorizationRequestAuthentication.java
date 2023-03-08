package com.generatera.authorization.application.server.config.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @author FLJ
 * @date 2023/2/1
 * @time 16:33
 * @Description 授权请求token(需要浏览器跳转的) ...
 */
public interface AuthorizationRequestAuthentication extends Authentication {


    default boolean needRedirect() {
        return false;
    }
    /**
     * 发送重定向
     * @param request request
     * @param response response
     */
    default void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 不做任何事情 ..
    }

    // 以下属性 仅作填充 ...
    @Override
    default Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    default Object getCredentials() {
        return null;
    }

    @Override
    default Object getDetails() {
        return null;
    }

    @Override
    default Object getPrincipal() {
        return null;
    }

    @Override
    default boolean isAuthenticated() {
        return false;
    }

    @Override
    default void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }


    @Override
    default String getName() {
        return null;
    }
}
