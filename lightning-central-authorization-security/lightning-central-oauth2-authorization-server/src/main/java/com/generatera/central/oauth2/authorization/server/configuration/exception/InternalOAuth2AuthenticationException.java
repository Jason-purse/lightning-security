package com.generatera.central.oauth2.authorization.server.configuration.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

/**
 * @author Sun.
 */
public class InternalOAuth2AuthenticationException extends OAuth2AuthenticationException {
    public InternalOAuth2AuthenticationException(AuthenticationException cause) {
        super(null, cause);
    }
}
