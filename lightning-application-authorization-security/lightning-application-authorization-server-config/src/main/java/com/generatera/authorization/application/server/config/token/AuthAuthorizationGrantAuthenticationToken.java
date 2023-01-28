package com.generatera.authorization.application.server.config.token;

import com.generatera.authorization.server.common.configuration.AuthorizationGrantType;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AuthAuthorizationGrantAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthorizationGrantType authorizationGrantType;
    private final Authentication clientPrincipal;
    private final Map<String, Object> additionalParameters;

    protected AuthAuthorizationGrantAuthenticationToken(AuthorizationGrantType authorizationGrantType, Authentication clientPrincipal, @Nullable Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        Assert.notNull(authorizationGrantType, "authorizationGrantType cannot be null");
        Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
        this.authorizationGrantType = authorizationGrantType;
        this.clientPrincipal = clientPrincipal;
        this.additionalParameters = Collections.unmodifiableMap(additionalParameters != null ? new HashMap<>(additionalParameters) : Collections.emptyMap());
    }

    public AuthorizationGrantType getGrantType() {
        return this.authorizationGrantType;
    }

    public Object getPrincipal() {
        return this.clientPrincipal;
    }

    public Object getCredentials() {
        return "";
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

}
