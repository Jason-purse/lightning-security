package com.generatera.security.authorization.server.specification.components.token.format.jwt;

import com.generatera.security.authorization.server.specification.components.token.LightningTokenContext;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.JwsHeader;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/11
 * @time 16:06
 * @Description Jwt 编码上下文 ...
 *
 * 在central-auth2-authorization-server中,这个将会代理到 {@link org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext}
 * 所以,任何东西都应该从spring oauth2 实现上进行获取,当前
 */
public  class JwtEncodingContext implements LightningTokenContext {
    private final Map<Object, Object> context;

    protected JwtEncodingContext(Map<Object, Object> context) {
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

    @Override
    public Map<Object, Object> getContexts() {
        return context;
    }

    public JwsHeader.Builder getHeaders() {
        return this.get(JwsHeader.Builder.class);
    }

    public JwtClaimsSet.Builder getClaims() {
        return this.get(JwtClaimsSet.Builder.class);
    }

    public static Builder with(JwsHeader.Builder headersBuilder, JwtClaimsSet.Builder claimsBuilder) {
        return new Builder(headersBuilder, claimsBuilder);
    }

    public static final class Builder extends AbstractBuilder<JwtEncodingContext, Builder> {

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