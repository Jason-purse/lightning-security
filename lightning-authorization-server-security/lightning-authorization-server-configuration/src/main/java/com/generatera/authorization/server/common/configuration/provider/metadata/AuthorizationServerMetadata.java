package com.generatera.authorization.server.common.configuration.provider.metadata;

import org.springframework.util.Assert;

import java.util.Map;

public final class AuthorizationServerMetadata extends AbstractAuthorizationServerMetadata {
    private AuthorizationServerMetadata(Map<String, Object> claims) {
        super(claims);
    }

    public static AuthorizationServerMetadata.Builder builder() {
        return new AuthorizationServerMetadata.Builder();
    }

    public static AuthorizationServerMetadata.Builder withClaims(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        return (new Builder()).claims((c) -> c.putAll(claims));
    }

    public static class Builder extends AbstractBuilder<AuthorizationServerMetadata, AuthorizationServerMetadata.Builder> {
        private Builder() {
        }

        public AuthorizationServerMetadata build() {
            this.validate();
            return new AuthorizationServerMetadata(this.getClaims());
        }
    }
}