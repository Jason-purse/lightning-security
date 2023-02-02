package com.generatera.authorization.application.server.config.token;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class DelegatingAuthenticationConverter implements AuthenticationConverter {
    private final List<AuthenticationConverter> converters;

    public DelegatingAuthenticationConverter(List<AuthenticationConverter> converters) {
        Assert.notEmpty(converters, "converters cannot be empty");
        this.converters = Collections.unmodifiableList(new LinkedList<>(converters));
    }

    @Nullable
    public Authentication convert(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");
        Iterator var2 = this.converters.iterator();

        Authentication authentication;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            AuthenticationConverter converter = (AuthenticationConverter)var2.next();
            authentication = converter.convert(request);
        } while(authentication == null);

        return authentication;
    }
}