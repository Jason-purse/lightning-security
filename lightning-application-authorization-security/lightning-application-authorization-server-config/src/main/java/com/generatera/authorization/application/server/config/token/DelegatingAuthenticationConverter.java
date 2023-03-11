package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.authorization.LightningAuthenticationConverter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class DelegatingAuthenticationConverter implements LightningAuthenticationConverter {
    private final List<LightningAuthenticationConverter> converters;

    public DelegatingAuthenticationConverter(List<LightningAuthenticationConverter> converters) {
        Assert.notEmpty(converters, "converters cannot be empty");
        this.converters = Collections.unmodifiableList(new LinkedList<>(converters));
    }

    @Nullable
    public Authentication convert(HttpServletRequest request, HttpServletResponse response) {
        Assert.notNull(request, "request cannot be null");
        Iterator var2 = this.converters.iterator();

        Authentication authentication;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            LightningAuthenticationConverter converter = (LightningAuthenticationConverter)var2.next();
            authentication = converter.convert(request,response);
        } while(authentication == null);

        return authentication;
    }
}