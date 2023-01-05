package com.generatera.resource.server.specification.token.jwt.config;

import org.springframework.util.Assert;

import java.util.Map;

public final class JwsHeader extends JoseHeader {

    private JwsHeader(Map<String, Object> headers) {
        super(headers);
    }

    public JwsAlgorithm getAlgorithm() {
        return (JwsAlgorithm)super.getAlgorithm();
    }

    public static Builder with(JwsAlgorithm jwsAlgorithm) {
        return new Builder(jwsAlgorithm);
    }

    public static Builder from(JwsHeader headers) {
        return new Builder(headers);
    }

    public static Builder from(Map<String,Object> headers) {
        return new Builder(headers);
    }

    public static final class Builder extends AbstractBuilder<JwsHeader, Builder> {
        private Builder(JwsAlgorithm jwsAlgorithm) {
            Assert.notNull(jwsAlgorithm, "jwsAlgorithm cannot be null");
            this.algorithm(jwsAlgorithm);
        }

        private Builder(JwsHeader headers) {
            Assert.notNull(headers, "headers cannot be null");
            this.getHeaders().putAll(headers.getHeaders());
        }

        private Builder(Map<String,Object> headers) {
            Assert.notNull(headers, "headers cannot be null");
            this.getHeaders().putAll(headers);
        }

        public JwsHeader build() {
            return new JwsHeader(this.getHeaders());
        }
    }
}