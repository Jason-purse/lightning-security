package com.generatera.security.authorization.server.specification;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * @author FLJ
 * @date 2023/1/11
 * @time 17:12
 * @Description lightning user context 上下文
 *
 * 通过此接口进行 spring SecurityContext 上下文解耦 ...
 */
public interface LightningUserContext {


    Optional<LightningUserPrincipal> getUserPrincipal();

    /**
     * 直接获取当前上下文 ...
     * 此用户上下文与线程绑定 ...
     */
    static LightningUserContext get() {
        return new DefaultLightningUserContext();
    }
}

class DefaultLightningUserContext implements LightningUserContext {

    private final LightningUserPrincipal userPrincipal;

    public DefaultLightningUserContext() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            this.userPrincipal = (LightningUserPrincipal) authentication
                    .getPrincipal();
        } else {
            this.userPrincipal = null;
        }
    }

    @Override
    public Optional<LightningUserPrincipal> getUserPrincipal() {
        return Optional.ofNullable(userPrincipal);
    }
}
