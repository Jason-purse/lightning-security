package com.generatera.authorization.application.server.config.specification;

import org.springframework.util.Assert;

import java.util.Map;

public final class OAuth2AuthorizationServerMetadata extends AbstractOAuth2AuthorizationServerMetadata {
    private OAuth2AuthorizationServerMetadata(Map<String, Object> claims) {
        super(claims);
    }

    public static OAuth2AuthorizationServerMetadata.Builder builder() {
        return new OAuth2AuthorizationServerMetadata.Builder();
    }

    public static OAuth2AuthorizationServerMetadata.Builder withClaims(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        return (OAuth2AuthorizationServerMetadata.Builder)(new OAuth2AuthorizationServerMetadata.Builder()).claims((c) -> {
            c.putAll(claims);
        });
    }

    public static class Builder extends AbstractBuilder<OAuth2AuthorizationServerMetadata, OAuth2AuthorizationServerMetadata.Builder> {
        private Builder() {
        }

        public OAuth2AuthorizationServerMetadata build() {
            this.validate();
            return new OAuth2AuthorizationServerMetadata(this.getClaims());
        }
    }
}