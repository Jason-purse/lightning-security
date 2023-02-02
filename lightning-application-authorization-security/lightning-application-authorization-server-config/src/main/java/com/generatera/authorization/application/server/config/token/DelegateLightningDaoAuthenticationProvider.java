package com.generatera.authorization.application.server.config.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DelegateLightningDaoAuthenticationProvider implements LightningDaoAuthenticationProvider {

    private final List<LightningDaoAuthenticationProvider> providers = new LinkedList<>();

    public DelegateLightningDaoAuthenticationProvider(LightningDaoAuthenticationProvider... providers) {
        this(List.of(providers));
    }
    public DelegateLightningDaoAuthenticationProvider(Collection<LightningDaoAuthenticationProvider> providers) {
        this.providers.addAll(providers);
    }
    @Override
    public Authentication authenticate(AuthAccessTokenAuthenticationToken authentication) throws AuthenticationException {
        for (LightningDaoAuthenticationProvider provider : providers) {
            Authentication authenticate = provider.authenticate(authentication);
            if(authenticate != null) {
                return authenticate;
            }
        }
        return null;
    }

}
