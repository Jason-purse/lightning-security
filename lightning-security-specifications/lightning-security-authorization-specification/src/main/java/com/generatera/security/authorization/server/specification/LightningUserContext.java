package com.generatera.security.authorization.server.specification;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * @author FLJ
 * @date 2023/1/11
 * @time 17:12
 * @Description lightning user context 上下文
 *
 * 通过此接口进行 spring SecurityContext 上下文解耦 ...
 *
 * 主要核心就是从{@code SecurityContext}中获取 {@code Authentication},从而获取到
 * 应用系统中使用的 {@code LightningUserContext}
 */
public interface LightningUserContext {


    Optional<LightningUserPrincipal> getUserPrincipal();

    /**
     * 获取指定 LightningUserPrincipal
     */
    <T extends LightningUserPrincipal> Optional<T> getUserPrincipal(Class<T> principalClass);

    /**
     * 直接获取当前上下文 ...
     * 此用户上下文与线程绑定 ...
     */
    static LightningUserContext get() {
        return new DefaultLightningUserContext();
    }

    static LightningUserContext set(LightningUserPrincipal userPrincipal) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (userPrincipal.isAuthenticated()) {
            context.setAuthentication(new UsernamePasswordAuthenticationToken(userPrincipal,null,userPrincipal.getAuthorities()));
        }
        else {
            context.setAuthentication(new AnonymousAuthenticationToken(userPrincipal.getName(), userPrincipal,userPrincipal.getAuthorities()));
        }
        SecurityContextHolder.setContext(context);
        return new DefaultLightningUserContext(userPrincipal);
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

    public DefaultLightningUserContext(LightningUserPrincipal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Optional<LightningUserPrincipal> getUserPrincipal() {
        return Optional.ofNullable(userPrincipal);
    }

    @Override
    public <T extends LightningUserPrincipal> Optional<T> getUserPrincipal(Class<T> principalClass) {
        return getUserPrincipal()
                .map(principalClass::cast);
    }
}
