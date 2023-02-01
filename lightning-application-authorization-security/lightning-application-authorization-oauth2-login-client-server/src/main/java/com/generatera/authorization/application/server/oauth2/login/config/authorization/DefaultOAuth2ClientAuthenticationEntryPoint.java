package com.generatera.authorization.application.server.oauth2.login.config.authorization;

import com.generatera.authorization.application.server.config.authentication.DefaultLightningAbstractAuthenticationEntryPoint;
import com.generatera.authorization.application.server.config.token.AppAuthServerForTokenAuthenticationProvider;
import com.generatera.authorization.application.server.config.token.AuthAccessTokenAuthenticationToken;
import com.generatera.authorization.application.server.oauth2.login.config.token.LightningOAuth2AuthenticationEntryPoint;
import com.generatera.security.authorization.server.specification.LightningUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author FLJ
 * @date 2023/2/1
 * @time 16:58
 * @Description 实现 token 颁发 ...
 *
 * 客户端的配置元数据 必须提供  tokenValueFormat ....
 */
public class DefaultOAuth2ClientAuthenticationEntryPoint extends DefaultLightningAbstractAuthenticationEntryPoint implements LightningOAuth2AuthenticationEntryPoint {

    private final AppAuthServerForTokenAuthenticationProvider authenticationProvider;

    public DefaultOAuth2ClientAuthenticationEntryPoint(AppAuthServerForTokenAuthenticationProvider authenticationProvider) {
        Assert.notNull(authenticationProvider,"authentication provider must not be null !!!");
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 构建认证 token
        LightningUserPrincipal userPrincipal = (LightningUserPrincipal) authentication.getPrincipal();

        AuthAccessTokenAuthenticationToken accessTokenAuthenticationToken = new AuthAccessTokenAuthenticationToken(
                new UsernamePasswordAuthenticationToken(userPrincipal, "", userPrincipal.getAuthorities()),
                null);
        AuthAccessTokenAuthenticationToken tokenAuthenticationToken = authenticationProvider.authenticate(accessTokenAuthenticationToken);

        super.onAuthenticationSuccess(request, response, tokenAuthenticationToken);
    }
}
