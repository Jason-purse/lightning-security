package com.generatera.security.authorization.server.specification.components.token;

import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public final class LightningTokenClaimsContext implements LightningTokenContext {
    private final Map<Object, Object> context;

    private LightningTokenClaimsContext(Map<Object, Object> context) {
        this.context = Map.copyOf(context);
    }

    @Nullable
    public <V> V get(Object key) {
        return this.hasKey(key) ? (V)this.context.get(key) : null;
    }

    public boolean hasKey(Object key) {
        Assert.notNull(key, "key cannot be null");
        return this.context.containsKey(key);
    }

    public LightningTokenClaimsSet.Builder getClaims() {
        return (LightningTokenClaimsSet.Builder)this.get(LightningTokenClaimsSet.Builder.class);
    }

    public static Builder with(LightningTokenClaimsSet.Builder claimsBuilder) {
        return new Builder(claimsBuilder);
    }

    public static final class Builder extends LightningTokenContext.AbstractBuilder<LightningTokenClaimsContext, Builder> {
        private Builder(LightningTokenClaimsSet.Builder claimsBuilder) {
            Assert.notNull(claimsBuilder, "claimsBuilder cannot be null");
            this.put(LightningTokenClaimsSet.Builder.class, claimsBuilder);
        }

        public LightningTokenClaimsContext build() {
            return new LightningTokenClaimsContext(this.getContext());
        }
    }
}
