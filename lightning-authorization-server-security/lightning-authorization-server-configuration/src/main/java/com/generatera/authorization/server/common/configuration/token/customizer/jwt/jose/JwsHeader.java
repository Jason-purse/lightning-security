package com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose;

import com.generatera.authorization.server.common.configuration.token.customizer.jwt.JwsAlgorithm;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

public final class JwsHeader extends JoseHeader {

    private JwsHeader(Map<String, Object> headers) {
        super(headers);
    }

    public JwsAlgorithm getAlgorithm() {
        return (JwsAlgorithm)super.getAlgorithm();
    }

    public static JwsHeader.Builder with(JwsAlgorithm jwsAlgorithm) {
        return new JwsHeader.Builder(jwsAlgorithm);
    }

    public static JwsHeader.Builder from(JwsHeader headers) {
        return new JwsHeader.Builder(headers);
    }

    public static JwsHeader.Builder from(Map<String,Object> headers) {
        return new JwsHeader.Builder(headers);
    }

    public static final class Builder extends AbstractBuilder<JwsHeader, JwsHeader.Builder> {
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