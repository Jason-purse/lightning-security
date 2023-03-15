package com.generatera.authorization.application.server.config.authentication;

import com.generatera.authorization.application.server.config.token.AppAuthServerForTokenAuthenticationProvider;
import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.authorization.application.server.config.token.LightningDaoAuthenticationProvider;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
 *
 * 形成一个约定,当没有提供login_grant_type的时候,(如果是{@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter})
 * 那么默认将产生 UsernamePasswordAuthentication ,同样也会包装为 AuthAccessTokenAuthenticationToken,通过{@link AuthAccessTokenAuthenticationToken#getAuthentication()}
 * 获取真实的 authentication ..
 *
 * 其他情况,将自动包装为 AuthAccessTokenAuthenticationToken
 */
public class LightningAppAuthServerDaoLoginAuthenticationProvider implements AuthenticationProvider {
    /**
     * 默认是false
     */
    private final boolean isSeparation;
    private final LightningDaoAuthenticationProvider authenticationManager;

    private AppAuthServerForTokenAuthenticationProvider authAccessAuthenticationProvider;

    public LightningAppAuthServerDaoLoginAuthenticationProvider(
            AppAuthServerForTokenAuthenticationProvider authenticationProvider,
            LightningDaoAuthenticationProvider authenticationManager,
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
            LightningDaoAuthenticationProvider authenticationManager
    ) {
        this(authenticationProvider, authenticationManager, false);
    }



    public void setAuthAccessAuthenticationProvider(AppAuthServerForTokenAuthenticationProvider authAccessAuthenticationProvider) {
        Assert.notNull(authAccessAuthenticationProvider, "authentication provider must not be null !!!");
        this.authAccessAuthenticationProvider = authAccessAuthenticationProvider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            AuthAccessTokenAuthenticationToken token = null;
            if(authentication instanceof AuthAccessTokenAuthenticationToken tokenAuthenticationToken) {
                token = tokenAuthenticationToken;
            }
            else {
                if(authentication instanceof UsernamePasswordAuthenticationToken authenticationToken) {
                    // 包装过多的问题 ..
                    if (authenticationToken.getPrincipal() instanceof Authentication internalAuthentication) {
                        token = new AuthAccessTokenAuthenticationToken(internalAuthentication);
                    }
                }
                if(token == null) {
                    token = new AuthAccessTokenAuthenticationToken(authentication,null);
                }
            }
            Authentication authenticate = authenticationManager.authenticate(token);

            if(authenticate == null) {
                throw new InternalAuthenticationServiceException("invalid_request,unsupported login grant type  !!!");
            }

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
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication) || AuthAccessTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
