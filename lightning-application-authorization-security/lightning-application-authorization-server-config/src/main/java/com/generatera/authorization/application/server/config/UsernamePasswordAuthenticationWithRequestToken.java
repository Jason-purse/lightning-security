package com.generatera.authorization.application.server.config;

import com.generatera.authorization.application.server.config.securityContext.DefaultLightningAuthentication;
import com.generatera.authorization.application.server.config.securityContext.LightningAuthentication;
import com.generatera.authorization.application.server.config.securityContext.LightningAuthenticationParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;


/**
 * @author Sun.
 */
public class UsernamePasswordAuthenticationWithRequestToken extends DefaultLightningAuthentication {

    private Authentication requestAuthentication;

    private Authentication authentication;

    public UsernamePasswordAuthenticationWithRequestToken(Authentication authentication,Authentication requestAuthentication) {
        super(authentication);
        this.requestAuthentication = requestAuthentication;

    }

    /**
     *  获取之前的请求形成的token
     */
    public Authentication getRequestAuthentication() {
        return requestAuthentication;
    }
}
