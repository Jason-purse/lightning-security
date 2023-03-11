package com.generatera.authorization.server.common.configuration.authorization;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LightningAuthenticationConverter {
    Authentication convert(HttpServletRequest request, HttpServletResponse response);
}
