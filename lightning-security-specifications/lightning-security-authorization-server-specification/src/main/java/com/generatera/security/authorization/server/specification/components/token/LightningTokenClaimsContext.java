package com.generatera.security.authorization.server.specification.components.token;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/16
 * @time 14:32
 * @Description 包含了各种上下文  以及 claims 信息
 *
 * 能够用来定制 token
 * @see LightningTokenCustomizer
 */
public final class LightningTokenClaimsContext implements LightningTokenContext {
    private final Map<Object, Object> context;

    private LightningTokenClaimsContext(Map<Object, Object> context) {
        this.context = Map.copyOf(context);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <V> V get(Object key) {
        return this.hasKey(key) ? (V)this.context.get(key) : null;
    }

    public boolean hasKey(Object key) {
        Assert.notNull(key, "key cannot be null");
        return this.context.containsKey(key);
    }

    /**
     * 获取一个构建器,用来构建 token claims ...
     */
    public LightningTokenClaimsSet.Builder getClaims() {
        return this.get(LightningTokenClaimsSet.Builder.class);
    }

    public static Builder with(LightningTokenClaimsSet.Builder claimsBuilder) {
        return new Builder(claimsBuilder);
    }

    @Override
    public Map<Object, Object> getContexts() {
        return context;
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
