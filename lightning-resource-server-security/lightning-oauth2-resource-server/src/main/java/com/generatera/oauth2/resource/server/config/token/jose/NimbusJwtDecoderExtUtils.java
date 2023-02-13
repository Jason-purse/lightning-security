package com.generatera.oauth2.resource.server.config.token.jose;

import com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.Jwks;
import com.generatera.security.authorization.server.specification.util.RsaKeyConversionUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NimbusJwtDecoderExtUtils {

    public static NimbusJwtDecoder fromJwkSource(JWKSource<SecurityContext> jwkSource, OAuth2ResourceServerProperties.Jwt properties) {
        NimbusJwtDecoder jwtDecoder = new JwkSourceJwtDecoderBuilder(jwkSource).build();
        if (StringUtils.hasText(properties.getIssuerUri())) {
            Supplier<OAuth2TokenValidator<Jwt>> defaultValidator = properties.getIssuerUri() != null ? () -> {
                return JwtValidators.createDefaultWithIssuer(properties.getIssuerUri());
            } : JwtValidators::createDefault;
            jwtDecoder.setJwtValidator(getValidators(defaultValidator, properties));
        }
        return jwtDecoder;
    }

    public static NimbusJwtDecoder fromPublicRsaKey(String publicKey) {
        return NimbusJwtDecoder.withPublicKey(RsaKeyConversionUtils.convertRsaPublicKey(publicKey)).build();
    }

    public static NimbusJwtDecoder fromSecretKey(String secretKey, String algorithm) {
        return NimbusJwtDecoder
                .withSecretKey(
                        Jwks.forSecretKey(secretKey, algorithm)
                ).build();
    }


    private static OAuth2TokenValidator<Jwt> getValidators(Supplier<OAuth2TokenValidator<Jwt>> defaultValidator, OAuth2ResourceServerProperties.Jwt properties) {
        OAuth2TokenValidator<Jwt> defaultValidators = defaultValidator.get();
        List<String> audiences = properties.getAudiences();
        if (CollectionUtils.isEmpty(audiences)) {
            return defaultValidators;
        } else {
            List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
            validators.add(defaultValidators);
            validators.add(new JwtClaimValidator<Collection<?>>("aud",
                    (aud) -> aud != null && !Collections.disjoint(aud, audiences)));

            return new DelegatingOAuth2TokenValidator<>(validators);
        }
    }

    public static final class JwkSourceJwtDecoderBuilder {
        private final JWKSource<SecurityContext> jwkSource;
        private final Set<SignatureAlgorithm> signatureAlgorithms;
        private Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer;

        private JwkSourceJwtDecoderBuilder(JWKSource<SecurityContext> jwkSource) {
            this.signatureAlgorithms = new HashSet<>();
            Assert.notNull(jwkSource, "jwkSource cannot be empty");
            this.jwkSource = jwkSource;
            this.jwtProcessorCustomizer = (processor) -> {
            };
        }

        public JwkSourceJwtDecoderBuilder jwsAlgorithm(SignatureAlgorithm signatureAlgorithm) {
            Assert.notNull(signatureAlgorithm, "signatureAlgorithm cannot be null");
            this.signatureAlgorithms.add(signatureAlgorithm);
            return this;
        }

        public JwkSourceJwtDecoderBuilder jwsAlgorithms(Consumer<Set<SignatureAlgorithm>> signatureAlgorithmsConsumer) {
            Assert.notNull(signatureAlgorithmsConsumer, "signatureAlgorithmsConsumer cannot be null");
            signatureAlgorithmsConsumer.accept(this.signatureAlgorithms);
            return this;
        }


        public JwkSourceJwtDecoderBuilder jwtProcessorCustomizer(Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer) {
            Assert.notNull(jwtProcessorCustomizer, "jwtProcessorCustomizer cannot be null");
            this.jwtProcessorCustomizer = jwtProcessorCustomizer;
            return this;
        }

        JWSKeySelector<SecurityContext> jwsKeySelector(JWKSource<SecurityContext> jwkSource) {
            if (this.signatureAlgorithms.isEmpty()) {
                return new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
            } else {
                Set<JWSAlgorithm> jwsAlgorithms = new HashSet<>();
                Iterator<SignatureAlgorithm> var3 = this.signatureAlgorithms.iterator();

                while (var3.hasNext()) {
                    SignatureAlgorithm signatureAlgorithm = var3.next();
                    JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(signatureAlgorithm.getName());
                    jwsAlgorithms.add(jwsAlgorithm);
                }

                return new JWSVerificationKeySelector<>(jwsAlgorithms, jwkSource);
            }
        }

        JWTProcessor<SecurityContext> processor() {
            JWKSource<SecurityContext> jwkSource = this.jwkSource;
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<SecurityContext>();
            jwtProcessor.setJWSKeySelector(this.jwsKeySelector(jwkSource));
            jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
            });
            this.jwtProcessorCustomizer.accept(jwtProcessor);
            return jwtProcessor;
        }

        public NimbusJwtDecoder build() {
            return new NimbusJwtDecoder(this.processor());
        }

//        private static final class SpringJWKSetCache implements JWKSetCache {
//            private final String jwkSetUri;
//            private final Cache cache;
//            private JWKSet jwkSet;
//
//            SpringJWKSetCache(String jwkSetUri, Cache cache) {
//                this.jwkSetUri = jwkSetUri;
//                this.cache = cache;
//                this.updateJwkSetFromCache();
//            }
//
//            private void updateJwkSetFromCache() {
//                String cachedJwkSet = (String)this.cache.get(this.jwkSetUri, String.class);
//                if (cachedJwkSet != null) {
//                    try {
//                        this.jwkSet = JWKSet.parse(cachedJwkSet);
//                    } catch (ParseException var3) {
//                    }
//                }
//
//            }
//
//            public void put(JWKSet jwkSet) {
//                this.jwkSet = jwkSet;
//                this.cache.put(this.jwkSetUri, jwkSet.toString(false));
//            }
//
//            public JWKSet get() {
//                return !this.requiresRefresh() ? this.jwkSet : null;
//            }
//
//            public boolean requiresRefresh() {
//                return this.cache.get(this.jwkSetUri) == null;
//            }
//        }
    }


}
