package com.generatera.central.oauth2.authorization.server.configuration.components.token;

import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.core.http.converter.OAuth2TokenIntrospectionHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @author FLJ
 * @date 2023/1/13
 * @time 9:53
 * @Description token 检查 响应处理器
 */
public class DefaultTokenIntrospectResponseHandler implements TokenIntrospectResponseHandler {
    private OAuth2TokenIntrospectionHttpMessageConverter tokenIntrospectionHttpMessageConverter
            = new OAuth2TokenIntrospectionHttpMessageConverter();


    public void setTokenIntrospectionHttpMessageConverter(OAuth2TokenIntrospectionHttpMessageConverter tokenIntrospectionHttpMessageConverter) {
        Assert.notNull(tokenIntrospectionHttpMessageConverter,"tokenIntrospectionHttpMessageConverter must not be null !!!");
        this.tokenIntrospectionHttpMessageConverter = tokenIntrospectionHttpMessageConverter;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2TokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication = (OAuth2TokenIntrospectionAuthenticationToken)authentication;
        OAuth2TokenIntrospection tokenClaims = tokenIntrospectionAuthentication.getTokenClaims();
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        this.tokenIntrospectionHttpMessageConverter.write(tokenClaims, null, httpResponse);
    }
}
