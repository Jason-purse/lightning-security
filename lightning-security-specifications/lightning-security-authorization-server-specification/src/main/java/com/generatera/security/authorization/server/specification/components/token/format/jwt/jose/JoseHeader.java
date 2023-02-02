package com.generatera.security.authorization.server.specification.components.token.format.jwt.jose;

import com.generatera.security.authorization.server.specification.components.token.JwaAlgorithm;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.converter.ClaimConversionService;
import org.springframework.util.Assert;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

class JoseHeader {
    private final Map<String, Object> headers;

    protected JoseHeader(Map<String, Object> headers) {
        Assert.notEmpty(headers, "headers cannot be empty");
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
    }

    public <T extends JwaAlgorithm> T getAlgorithm() {
        return (T) this.getHeader("alg");
    }

    public URL getJwkSetUrl() {
        return (URL) this.getHeader("jku");
    }

    public Map<String, Object> getJwk() {
        return (Map) this.getHeader("jwk");
    }

    public String getKeyId() {
        return (String) this.getHeader("kid");
    }

    public URL getX509Url() {
        return (URL) this.getHeader("x5u");
    }

    public List<String> getX509CertificateChain() {
        return (List) this.getHeader("x5c");
    }

    public String getX509SHA1Thumbprint() {
        return (String) this.getHeader("x5t");
    }

    public String getX509SHA256Thumbprint() {
        return (String) this.getHeader("x5t#S256");
    }

    public String getType() {
        return (String) this.getHeader("typ");
    }

    public String getContentType() {
        return (String) this.getHeader("cty");
    }

    public Set<String> getCritical() {
        return (Set) this.getHeader("crit");
    }

    public Map<String, Object> getHeaders() {
        return this.headers;
    }

    public <T> T getHeader(String name) {
        Assert.hasText(name, "name cannot be empty");
        return (T) this.getHeaders().get(name);
    }

    abstract static class AbstractBuilder<T extends JoseHeader, B extends AbstractBuilder<T, B>> {
        private final Map<String, Object> headers = new HashMap<>();

        protected AbstractBuilder() {
        }

        protected Map<String, Object> getHeaders() {
            return this.headers;
        }

        protected final B getThis() {
            return (B) this;
        }

        public B algorithm(JwaAlgorithm jwaAlgorithm) {
            return this.header("alg", jwaAlgorithm);
        }

        public B jwkSetUrl(String jwkSetUrl) {
            return this.header("jku", convertAsURL("jku", jwkSetUrl));
        }

        public B jwk(Map<String, Object> jwk) {
            return this.header("jwk", jwk);
        }

        public B keyId(String keyId) {
            return this.header("kid", keyId);
        }

        public B x509Url(String x509Url) {
            return this.header("x5u", convertAsURL("x5u", x509Url));
        }

        public B x509CertificateChain(List<String> x509CertificateChain) {
            return this.header("x5c", x509CertificateChain);
        }

        public B x509SHA1Thumbprint(String x509SHA1Thumbprint) {
            return this.header("x5t", x509SHA1Thumbprint);
        }

        public B x509SHA256Thumbprint(String x509SHA256Thumbprint) {
            return this.header("x5t#S256", x509SHA256Thumbprint);
        }

        public B type(String type) {
            return this.header("typ", type);
        }

        public B contentType(String contentType) {
            return this.header("cty", contentType);
        }

        public B criticalHeader(String name, Object value) {
            this.header(name, value);
            this.getHeaders().computeIfAbsent("crit", (k) -> {
                return new HashSet();
            });
            ((Set) this.getHeaders().get("crit")).add(name);
            return this.getThis();
        }

        public B header(String name, Object value) {
            Assert.hasText(name, "name cannot be empty");
            Assert.notNull(value, "value cannot be null");
            this.headers.put(name, value);
            return this.getThis();
        }

        public B headers(Consumer<Map<String, Object>> headersConsumer) {
            headersConsumer.accept(this.headers);
            return this.getThis();
        }

        public abstract T build();

        private static URL convertAsURL(String header, String value) {
            URL convertedValue = (URL) ClaimConversionService.getSharedInstance().convert(value, URL.class);
            Assert.isTrue(convertedValue != null, () -> {
                return "Unable to convert header '" + header + "' of type '" + value.getClass() + "' to URL.";
            });
            return convertedValue;
        }
    }
}
