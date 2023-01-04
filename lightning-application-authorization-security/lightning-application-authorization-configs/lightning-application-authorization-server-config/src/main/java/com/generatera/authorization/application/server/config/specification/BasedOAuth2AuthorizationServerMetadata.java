package com.generatera.authorization.application.server.config.specification;

import org.springframework.util.Assert;

import java.util.Map;

public final class BasedOAuth2AuthorizationServerMetadata extends AbstractOAuth2AuthorizationServerMetadata {
    private BasedOAuth2AuthorizationServerMetadata(Map<String, Object> claims) {
        super(claims);
    }

    public static BasedOAuth2AuthorizationServerMetadata.Builder builder() {
        return new BasedOAuth2AuthorizationServerMetadata.Builder();
    }

    public static BasedOAuth2AuthorizationServerMetadata.Builder withClaims(Map<String, Object> claims) {
        Assert.notEmpty(claims, "claims cannot be empty");
        return (BasedOAuth2AuthorizationServerMetadata.Builder)(new BasedOAuth2AuthorizationServerMetadata.Builder()).claims((c) -> {
            c.putAll(claims);
        });
    }

    public static class Builder extends AbstractBuilder<BasedOAuth2AuthorizationServerMetadata, BasedOAuth2AuthorizationServerMetadata.Builder> {
        private Builder() {
        }

        public BasedOAuth2AuthorizationServerMetadata build() {
            this.validate();
            return new BasedOAuth2AuthorizationServerMetadata(this.getClaims());
        }
    }
}