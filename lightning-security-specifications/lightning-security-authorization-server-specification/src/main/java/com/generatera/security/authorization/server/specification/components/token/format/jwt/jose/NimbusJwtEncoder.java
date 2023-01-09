package com.generatera.security.authorization.server.specification.components.token.format.jwt.jose;

import com.generatera.security.authorization.server.specification.SignatureAlgorithm;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtClaimsSet;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.JwtEncoderParameters;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwt;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtEncoder;
import com.generatera.security.authorization.server.specification.components.token.format.jwt.exception.JwtEncodingException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.produce.JWSSignerFactory;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NimbusJwtEncoder implements LightningJwtEncoder {
    private static final String ENCODING_ERROR_MESSAGE_TEMPLATE = "An error occurred while attempting to encode the Jwt: %s";
    private static final JwsHeader DEFAULT_JWS_HEADER;
    private static final JWSSignerFactory JWS_SIGNER_FACTORY;
    private final Map<JWK, JWSSigner> jwsSigners = new ConcurrentHashMap<>();
    private final JWKSource<SecurityContext> jwkSource;

    public NimbusJwtEncoder(JWKSource<SecurityContext> jwkSource) {
        Assert.notNull(jwkSource, "jwkSource cannot be null");
        this.jwkSource = jwkSource;
    }

    public LightningJwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        Assert.notNull(parameters, "parameters cannot be null");
        JwsHeader headers = parameters.getJwsHeader();
        if (headers == null) {
            headers = DEFAULT_JWS_HEADER;
        }

        JwtClaimsSet claims = parameters.getClaims();
        JWK jwk = this.selectJwk(headers);
        headers = addKeyIdentifierHeadersIfNecessary(headers, jwk);
        String jws = this.serialize(headers, claims, jwk);
        return new LightningJwt(parameters.getTokenValueType(), jws, claims.getIssuedAt(), claims.getExpiresAt(), headers.getHeaders(), claims.getClaims());
    }

    private JWK selectJwk(JwsHeader headers) {
        List<JWK> jwks;
        try {
            JWKSelector jwkSelector = new JWKSelector(createJwkMatcher(headers));
            jwks = this.jwkSource.get(jwkSelector, (SecurityContext) null);
        } catch (Exception var4) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Failed to select a JWK signing key -> " + var4.getMessage()), var4);
        }

        if (jwks.size() > 1) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Found multiple JWK signing keys for algorithm '" + headers.getAlgorithm().getName() + "'"));
        } else if (jwks.isEmpty()) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Failed to select a JWK signing key"));
        } else {
            return (JWK) jwks.get(0);
        }
    }

    private String serialize(JwsHeader headers, JwtClaimsSet claims, JWK jwk) {
        JWSHeader jwsHeader = convert(headers);
        JWTClaimsSet jwtClaimsSet = convert(claims);
        JWSSigner jwsSigner = (JWSSigner) this.jwsSigners.computeIfAbsent(jwk, NimbusJwtEncoder::createSigner);
        SignedJWT signedJwt = new SignedJWT(jwsHeader, jwtClaimsSet);

        try {
            signedJwt.sign(jwsSigner);
        } catch (JOSEException var9) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Failed to sign the JWT -> " + var9.getMessage()), var9);
        }

        return signedJwt.serialize();
    }

    @Nullable
    private static JWKMatcher createJwkMatcher(JwsHeader headers) {
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(headers.getAlgorithm().getName());
        if (!JWSAlgorithm.Family.RSA.contains(jwsAlgorithm) && !JWSAlgorithm.Family.EC.contains(jwsAlgorithm)) {
            return JWSAlgorithm.Family.HMAC_SHA.contains(jwsAlgorithm) ? (new JWKMatcher.Builder()).keyType(KeyType.forAlgorithm(jwsAlgorithm)).keyID(headers.getKeyId()).privateOnly(true).algorithms(new Algorithm[]{jwsAlgorithm, null}).build() : null;
        } else {
            return (new JWKMatcher.Builder()).keyType(KeyType.forAlgorithm(jwsAlgorithm)).keyID(headers.getKeyId()).keyUses(KeyUse.SIGNATURE, null).algorithms(jwsAlgorithm, null).x509CertSHA256Thumbprint(Base64URL.from(headers.getX509SHA256Thumbprint())).build();
        }
    }

    private static JwsHeader addKeyIdentifierHeadersIfNecessary(JwsHeader headers, JWK jwk) {
        if (StringUtils.hasText(headers.getKeyId()) && StringUtils.hasText(headers.getX509SHA256Thumbprint())) {
            return headers;
        } else if (!StringUtils.hasText(jwk.getKeyID()) && jwk.getX509CertSHA256Thumbprint() == null) {
            return headers;
        } else {
            JwsHeader.Builder headersBuilder = JwsHeader.from(headers);
            if (!StringUtils.hasText(headers.getKeyId()) && StringUtils.hasText(jwk.getKeyID())) {
                headersBuilder.keyId(jwk.getKeyID());
            }

            if (!StringUtils.hasText(headers.getX509SHA256Thumbprint()) && jwk.getX509CertSHA256Thumbprint() != null) {
                headersBuilder.x509SHA256Thumbprint(jwk.getX509CertSHA256Thumbprint().toString());
            }

            return headersBuilder.build();
        }
    }

    private static JWSSigner createSigner(JWK jwk) {
        try {
            return JWS_SIGNER_FACTORY.createJWSSigner(jwk);
        } catch (JOSEException var2) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Failed to create a JWS Signer -> " + var2.getMessage()), var2);
        }
    }

    private static JWSHeader convert(JwsHeader headers) {
        JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.parse(headers.getAlgorithm().getName()));
        if (headers.getJwkSetUrl() != null) {
            builder.jwkURL(convertAsURI("jku", headers.getJwkSetUrl()));
        }

        Map<String, Object> jwk = headers.getJwk();
        if (!CollectionUtils.isEmpty(jwk)) {
            try {
                builder.jwk(JWK.parse(jwk));
            } catch (Exception var11) {
                throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Unable to convert 'jwk' JOSE header"), var11);
            }
        }

        String keyId = headers.getKeyId();
        if (StringUtils.hasText(keyId)) {
            builder.keyID(keyId);
        }

        if (headers.getX509Url() != null) {
            builder.x509CertURL(convertAsURI("x5u", headers.getX509Url()));
        }

        List<String> x509CertificateChain = headers.getX509CertificateChain();
        if (!CollectionUtils.isEmpty(x509CertificateChain)) {
            List<com.nimbusds.jose.util.Base64> x5cList = new ArrayList<>();
            x509CertificateChain.forEach((x5c) -> x5cList.add(new com.nimbusds.jose.util.Base64(x5c)));
            if (!x5cList.isEmpty()) {
                builder.x509CertChain(x5cList);
            }
        }

        String x509SHA1Thumbprint = headers.getX509SHA1Thumbprint();
        if (StringUtils.hasText(x509SHA1Thumbprint)) {
            builder.x509CertThumbprint(new Base64URL(x509SHA1Thumbprint));
        }

        String x509SHA256Thumbprint = headers.getX509SHA256Thumbprint();
        if (StringUtils.hasText(x509SHA256Thumbprint)) {
            builder.x509CertSHA256Thumbprint(new Base64URL(x509SHA256Thumbprint));
        }

        String type = headers.getType();
        if (StringUtils.hasText(type)) {
            builder.type(new JOSEObjectType(type));
        }

        String contentType = headers.getContentType();
        if (StringUtils.hasText(contentType)) {
            builder.contentType(contentType);
        }

        Set<String> critical = headers.getCritical();
        if (!CollectionUtils.isEmpty(critical)) {
            builder.criticalParams(critical);
        }

        Map<String, Object> customHeaders = new HashMap<>();
        headers.getHeaders().forEach((name, value) -> {
            if (!JWSHeader.getRegisteredParameterNames().contains(name)) {
                customHeaders.put(name, value);
            }

        });
        if (!customHeaders.isEmpty()) {
            builder.customParams(customHeaders);
        }

        return builder.build();
    }

    private static JWTClaimsSet convert(JwtClaimsSet claims) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        Object issuer = claims.getClaim("iss");
        if (issuer != null) {
            builder.issuer(issuer.toString());
        }

        String subject = claims.getSubject();
        if (StringUtils.hasText(subject)) {
            builder.subject(subject);
        }

        List<String> audience = claims.getAudience();
        if (!CollectionUtils.isEmpty(audience)) {
            builder.audience(audience);
        }

        Instant expiresAt = claims.getExpiresAt();
        if (expiresAt != null) {
            builder.expirationTime(Date.from(expiresAt));
        }

        Instant notBefore = claims.getNotBefore();
        if (notBefore != null) {
            builder.notBeforeTime(Date.from(notBefore));
        }

        Instant issuedAt = claims.getIssuedAt();
        if (issuedAt != null) {
            builder.issueTime(Date.from(issuedAt));
        }

        String jwtId = claims.getId();
        if (StringUtils.hasText(jwtId)) {
            builder.jwtID(jwtId);
        }

        Map<String, Object> customClaims = new HashMap<>();
        claims.getClaims().forEach((name, value) -> {
            if (!JWTClaimsSet.getRegisteredNames().contains(name)) {
                customClaims.put(name, value);
            }

        });
        if (!customClaims.isEmpty()) {
            Objects.requireNonNull(builder);
            customClaims.forEach(builder::claim);
        }

        return builder.build();
    }

    private static URI convertAsURI(String header, URL url) {
        try {
            return url.toURI();
        } catch (Exception var3) {
            throw new JwtEncodingException(String.format("An error occurred while attempting to encode the Jwt: %s", "Unable to convert '" + header + "' JOSE header to a URI"), var3);
        }
    }

    static {
        DEFAULT_JWS_HEADER = JwsHeader.with(SignatureAlgorithm.RS256).build();
        JWS_SIGNER_FACTORY = new DefaultJWSSignerFactory();
    }
}