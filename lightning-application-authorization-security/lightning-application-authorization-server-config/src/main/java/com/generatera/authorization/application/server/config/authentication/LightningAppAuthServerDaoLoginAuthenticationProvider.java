package com.generatera.authorization.application.server.config.authentication;

import com.generatera.authorization.application.server.config.token.AppAuthServerForTokenAuthenticationProvider;
import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

/**
 * @author FLJ
 * @date 2023/1/29
 * @time 9:31
 * @Description 主要负责将 用户登录校验交给认证管理器,并在合适的情况下,使用生成token的认证提供器生成token
 * <p>
 * 接管 ...
 */
public class LightningAppAuthServerDaoLoginAuthenticationProvider implements AuthenticationProvider {
    /**
     * 默认是false
     */
    private final boolean isSeparation;
    private final DaoAuthenticationProvider authenticationManager;

    public LightningAppAuthServerDaoLoginAuthenticationProvider(
            AppAuthServerForTokenAuthenticationProvider authenticationProvider,
            DaoAuthenticationProvider authenticationManager,
            boolean isSeparation
    ) {
        Assert.notNull(authenticationManager, "authenticationManager must not be null !!!");
        Assert.notNull(authenticationProvider, "authentication provider must not be null !!!");
        this.authAccessAuthenticationProvider = authenticationProvider;
        this.authenticationManager = authenticationManager;
        this.isSeparation = isSeparation;
    }

    public LightningAppAuthServerDaoLoginAuthenticationProvider(
            AppAuthServerForTokenAuthenticationProvider authenticationProvider,
            DaoAuthenticationProvider authenticationManager
    ) {
        this(authenticationProvider, authenticationManager, false);
    }

    private AppAuthServerForTokenAuthenticationProvider authAccessAuthenticationProvider;

    public void setAuthAccessAuthenticationProvider(AppAuthServerForTokenAuthenticationProvider authAccessAuthenticationProvider) {
        Assert.notNull(authAccessAuthenticationProvider, "authentication provider must not be null !!!");
        this.authAccessAuthenticationProvider = authAccessAuthenticationProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken loginAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
        try {
            Authentication authenticate = authenticationManager.authenticate(loginAuthenticationToken);

            if (isSeparation) {
                return authAccessAuthenticationProvider.authenticate(
                        new AuthAccessTokenAuthenticationToken(authenticate, null, null));

            }

            return authenticate;
        } catch (Exception e) {
            if(e instanceof AccountStatusException || e instanceof  InternalAuthenticationServiceException) {
                throw e;
            }
            // 必须抛出异常,而不应该让daoAuthenticationProvider 再次处理 ..
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
