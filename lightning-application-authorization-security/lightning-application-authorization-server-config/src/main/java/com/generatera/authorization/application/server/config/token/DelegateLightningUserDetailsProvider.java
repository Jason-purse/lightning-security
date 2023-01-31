package com.generatera.authorization.application.server.config.token;

import com.generatera.security.authorization.server.specification.LightningUserPrincipal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DelegateLightningUserDetailsProvider implements LightningUserDetailsProvider {

    private final List<LightningUserDetailsProvider> providers = new LinkedList<>();

    public DelegateLightningUserDetailsProvider(LightningUserDetailsProvider ... providers) {
        this(List.of(providers));
    }
    public DelegateLightningUserDetailsProvider(Collection<LightningUserDetailsProvider> providers) {
        this.providers.addAll(providers);
    }

    @Override
    public LightningUserPrincipal getUserDetails(AuthRefreshTokenAuthenticationToken authRefreshTokenAuthenticationToken,String principalName) {
        for (LightningUserDetailsProvider provider : providers) {
            LightningUserPrincipal userDetails = provider.getUserDetails(authRefreshTokenAuthenticationToken,principalName);
            if(userDetails != null) {
                return userDetails;
            }
        }
        return null;
    }
}
