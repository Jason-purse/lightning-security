package com.generatera.security.authorization.server.specification.components.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;

/**
 * 登出处理器 ..
 */
public interface LightningLogoutHandler extends LogoutHandler {


    /**
     * delegate handler
     */
    public static LightningLogoutHandler delegate(LightningLogoutHandler ... handlers) {
        return new DelegateLightningLogoutHandler(handlers);
    }

    /**
     * delegate handler
     */
    public static LightningLogoutHandler delegate(Collection<LightningLogoutHandler> handlers) {
        return new DelegateLightningLogoutHandler(handlers);
    }

}

class DelegateLightningLogoutHandler implements LightningLogoutHandler {
    private final Collection<LightningLogoutHandler> handlers;
    public DelegateLightningLogoutHandler(LightningLogoutHandler... handlers) {
        this.handlers = Arrays.asList(handlers);
    }

    public DelegateLightningLogoutHandler(Collection<LightningLogoutHandler> handlers) {
        this.handlers = handlers;
    }
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        for (LightningLogoutHandler handler : handlers) {
            handler.logout(request,response,authentication);
        }
    }
}
