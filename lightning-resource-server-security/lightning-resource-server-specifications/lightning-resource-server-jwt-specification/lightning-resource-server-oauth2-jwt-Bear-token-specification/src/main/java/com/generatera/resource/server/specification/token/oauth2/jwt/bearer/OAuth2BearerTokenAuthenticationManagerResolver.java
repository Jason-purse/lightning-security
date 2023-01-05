package com.generatera.resource.server.specification.token.oauth2.jwt.bearer;

import org.springframework.security.authentication.AuthenticationManagerResolver;

import javax.servlet.http.HttpServletRequest;

public interface OAuth2BearerTokenAuthenticationManagerResolver extends AuthenticationManagerResolver<HttpServletRequest> {
}
