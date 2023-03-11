package com.generatera.authorization.server.common.configuration.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DelegateAuthenticationConverter implements LightningAuthenticationConverter {
    private final LightningAuthenticationConverter converter;

    public DelegateAuthenticationConverter(LightningAuthenticationConverter converter) {
        Assert.notNull(converter,"converter must not be null");
        this.converter = converter;
    }
    @Override
    public Authentication convert(HttpServletRequest request, HttpServletResponse response) {
        return converter.convert(request,response);
    }
}
