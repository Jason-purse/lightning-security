package com.generatera.resource.server.config.token.entrypoint;

import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DelegateAuthenticationEntryPoint implements LightningAuthenticationEntryPoint {
    private final LightningAuthenticationEntryPoint entryPoint;
    public DelegateAuthenticationEntryPoint(LightningAuthenticationEntryPoint entryPoint) {
        Assert.notNull(entryPoint,"entryPoint must not be null !!!");
        this.entryPoint = entryPoint;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        entryPoint.onAuthenticationFailure(request,response,exception);
    }
}
