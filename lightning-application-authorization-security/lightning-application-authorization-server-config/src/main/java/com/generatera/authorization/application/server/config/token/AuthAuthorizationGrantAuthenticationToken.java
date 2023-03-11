package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.LightningAuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AuthAuthorizationGrantAuthenticationToken extends AbstractAuthenticationToken {

    private final LightningAuthorizationGrantType authorizationGrantType;
    private final Authentication principal;
    private final Map<String, Object> additionalParameters;

    protected AuthAuthorizationGrantAuthenticationToken(LightningAuthorizationGrantType authorizationGrantType, @Nullable Authentication principal, @Nullable Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        Assert.notNull(authorizationGrantType, "authorizationGrantType cannot be null");
        this.authorizationGrantType = authorizationGrantType;
        this.principal = principal;
        this.additionalParameters = Collections.unmodifiableMap(additionalParameters != null ? new HashMap<>(additionalParameters) : Collections.emptyMap());
    }

    public LightningAuthorizationGrantType getGrantType() {
        return this.authorizationGrantType;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public Object getCredentials() {
        return "";
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

}
