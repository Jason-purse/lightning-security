package com.generatera.authorization.server.common.configuration.token;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose.JwsHeader;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwtClaimsSet;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.Map;

public final class JwtEncodingContext implements LightningTokenContext {
    private final Map<Object, Object> context;

    private JwtEncodingContext(Map<Object, Object> context) {
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

    public JwsHeader.Builder getHeaders() {
        return this.get(JwsHeader.Builder.class);
    }

    public JwtClaimsSet.Builder getClaims() {
        return this.get(JwtClaimsSet.Builder.class);
    }

    public static JwtEncodingContext.Builder with(JwsHeader.Builder headersBuilder, JwtClaimsSet.Builder claimsBuilder) {
        return new JwtEncodingContext.Builder(headersBuilder, claimsBuilder);
    }

    public static final class Builder extends AbstractBuilder<JwtEncodingContext,JwtEncodingContext.Builder> {

        private Builder(JwsHeader.Builder headersBuilder, JwtClaimsSet.Builder claimsBuilder) {
            Assert.notNull(headersBuilder, "headersBuilder cannot be null");
            Assert.notNull(claimsBuilder, "claimsBuilder cannot be null");
            this.put(JwsHeader.Builder.class, headersBuilder);
            this.put(JwtClaimsSet.Builder.class, claimsBuilder);
        }


        public JwtEncodingContext build() {
            return new JwtEncodingContext(this.getContext());
        }
    }
}