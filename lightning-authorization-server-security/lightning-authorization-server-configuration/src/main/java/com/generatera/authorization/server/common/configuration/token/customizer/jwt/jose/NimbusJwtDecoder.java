package com.generatera.authorization.server.common.configuration.token.customizer.jwt.jose;

import com.generatera.authorization.server.common.configuration.token.LightningAuthError;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidator;
import com.generatera.authorization.server.common.configuration.token.LightningTokenValidatorResult;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.*;
import com.generatera.authorization.server.common.configuration.token.customizer.jwt.exception.JwtException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSetCache;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SingleKeyJWSKeySelector;
import com.nimbusds.jose.shaded.json.parser.ParseException;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.function.Consumer;

public final class NimbusJwtDecoder implements LightningJwtDecoder {
    private final Log logger = LogFactory.getLog(this.getClass());
    private static final String DECODING_ERROR_MESSAGE_TEMPLATE = "An error occurred while attempting to decode the Jwt: %s";
    private final JWTProcessor<SecurityContext> jwtProcessor;
    private Converter<Map<String, Object>, Map<String, Object>> claimSetConverter = MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());
    private LightningTokenValidator<LightningJwt> jwtValidator = JwtValidators.createDefault();

    public NimbusJwtDecoder(JWTProcessor<SecurityContext> jwtProcessor) {
        Assert.notNull(jwtProcessor, "jwtProcessor cannot be null");
        this.jwtProcessor = jwtProcessor;
    }

    public static NimbusJwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        Set<JWSAlgorithm> jwsAlgs = new HashSet<>();
        jwsAlgs.addAll(JWSAlgorithm.Family.RSA);
        jwsAlgs.addAll(JWSAlgorithm.Family.EC);
        jwsAlgs.addAll(JWSAlgorithm.Family.HMAC_SHA);
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(jwsAlgs, jwkSource);
        jwtProcessor.setJWSKeySelector(jwsKeySelector);
        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
        });
        return new NimbusJwtDecoder(jwtProcessor);
    }

    public void setJwtValidator(LightningTokenValidator<LightningJwt> jwtValidator) {
        Assert.notNull(jwtValidator, "jwtValidator cannot be null");
        this.jwtValidator = jwtValidator;
    }

    public void setClaimSetConverter(Converter<Map<String, Object>, Map<String, Object>> claimSetConverter) {
        Assert.notNull(claimSetConverter, "claimSetConverter cannot be null");
        this.claimSetConverter = claimSetConverter;
    }

    public LightningJwt decode(String token) throws JwtException {
        JWT jwt = this.parse(token);
        if (jwt instanceof PlainJWT) {
            this.logger.trace("Failed to decode unsigned token");
            throw new BadJwtException("Unsupported algorithm of " + jwt.getHeader().getAlgorithm());
        } else {
            LightningJwt createdJwt = this.createJwt(token, jwt);
            return this.validateJwt(createdJwt);
        }
    }

    private JWT parse(String token) {
        try {
            return JWTParser.parse(token);
        } catch (Exception var3) {
            this.logger.trace("Failed to parse token", var3);
            throw new BadJwtException(String.format("An error occurred while attempting to decode the Jwt: %s", var3.getMessage()), var3);
        }
    }

    private LightningJwt createJwt(String token, JWT parsedJwt) {
        try {
            JWTClaimsSet jwtClaimsSet = this.jwtProcessor.process(parsedJwt, (SecurityContext)null);
            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            Map<String, Object> claims = (Map)this.claimSetConverter.convert(jwtClaimsSet.getClaims());
            return DefaultLightningJwt.withTokenValue(token).headers((h) -> {
                h.putAll(headers);
            }).claims((c) -> {
                c.putAll(claims);
            }).build();
        } catch (RemoteKeySourceException var6) {
            this.logger.trace("Failed to retrieve JWK set", var6);
            if (var6.getCause() instanceof ParseException) {
                throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", "Malformed Jwk set"), var6);
            } else {
                throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", var6.getMessage()), var6);
            }
        } catch (JOSEException var7) {
            this.logger.trace("Failed to process JWT", var7);
            throw new JwtException(String.format("An error occurred while attempting to decode the Jwt: %s", var7.getMessage()), var7);
        } catch (Exception var8) {
            this.logger.trace("Failed to process JWT", var8);
            if (var8.getCause() instanceof ParseException) {
                throw new BadJwtException(String.format("An error occurred while attempting to decode the Jwt: %s", "Malformed payload"), var8);
            } else {
                throw new BadJwtException(String.format("An error occurred while attempting to decode the Jwt: %s", var8.getMessage()), var8);
            }
        }
    }

    private LightningJwt validateJwt(LightningJwt jwt) {
        LightningTokenValidatorResult result = this.jwtValidator.validate(jwt);
        if (result.hasErrors()) {
            Collection<LightningAuthError> errors = result.getErrors();
            String validationErrorString = this.getJwtValidationExceptionMessage(errors);
            throw new JwtValidationException(validationErrorString, errors);
        } else {
            return jwt;
        }
    }

    private String getJwtValidationExceptionMessage(Collection<LightningAuthError> errors) {
        Iterator var2 = errors.iterator();

        LightningAuthError authError;
        do {
            if (!var2.hasNext()) {
                return "Unable to validate Jwt";
            }

            authError = (LightningAuthError)var2.next();
        } while(StringUtils.isEmpty(authError.getDescription()));

        return String.format("An error occurred while attempting to decode the Jwt: %s", authError.getDescription());
    }

    public static JwkSetUriJwtDecoderBuilder withJwkSetUri(String jwkSetUri) {
        return new JwkSetUriJwtDecoderBuilder(jwkSetUri);
    }

    public static PublicKeyJwtDecoderBuilder withPublicKey(RSAPublicKey key) {
        return new PublicKeyJwtDecoderBuilder(key);
    }

    public static SecretKeyJwtDecoderBuilder withSecretKey(SecretKey secretKey) {
        return new SecretKeyJwtDecoderBuilder(secretKey);
    }

    public static final class SecretKeyJwtDecoderBuilder {
        private final SecretKey secretKey;
        private JWSAlgorithm jwsAlgorithm;
        private Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer;

        private SecretKeyJwtDecoderBuilder(SecretKey secretKey) {
            this.jwsAlgorithm = JWSAlgorithm.HS256;
            Assert.notNull(secretKey, "secretKey cannot be null");
            this.secretKey = secretKey;
            this.jwtProcessorCustomizer = (processor) -> {
            };
        }

        public SecretKeyJwtDecoderBuilder macAlgorithm(MacAlgorithm macAlgorithm) {
            Assert.notNull(macAlgorithm, "macAlgorithm cannot be null");
            this.jwsAlgorithm = JWSAlgorithm.parse(macAlgorithm.getName());
            return this;
        }

        public SecretKeyJwtDecoderBuilder jwtProcessorCustomizer(Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer) {
            Assert.notNull(jwtProcessorCustomizer, "jwtProcessorCustomizer cannot be null");
            this.jwtProcessorCustomizer = jwtProcessorCustomizer;
            return this;
        }

        public NimbusJwtDecoder build() {
            return new NimbusJwtDecoder(this.processor());
        }

        JWTProcessor<SecurityContext> processor() {
            JWSKeySelector<SecurityContext> jwsKeySelector = new SingleKeyJWSKeySelector(this.jwsAlgorithm, this.secretKey);
            DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor();
            jwtProcessor.setJWSKeySelector(jwsKeySelector);
            jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
            });
            this.jwtProcessorCustomizer.accept(jwtProcessor);
            return jwtProcessor;
        }
    }

    public static final class PublicKeyJwtDecoderBuilder {
        private JWSAlgorithm jwsAlgorithm;
        private RSAPublicKey key;
        private Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer;

        private PublicKeyJwtDecoderBuilder(RSAPublicKey key) {
            Assert.notNull(key, "key cannot be null");
            this.jwsAlgorithm = JWSAlgorithm.RS256;
            this.key = key;
            this.jwtProcessorCustomizer = (processor) -> {
            };
        }

        public PublicKeyJwtDecoderBuilder signatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
            Assert.notNull(signatureAlgorithm, "signatureAlgorithm cannot be null");
            this.jwsAlgorithm = JWSAlgorithm.parse(signatureAlgorithm.getName());
            return this;
        }

        public PublicKeyJwtDecoderBuilder jwtProcessorCustomizer(Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer) {
            Assert.notNull(jwtProcessorCustomizer, "jwtProcessorCustomizer cannot be null");
            this.jwtProcessorCustomizer = jwtProcessorCustomizer;
            return this;
        }

        JWTProcessor<SecurityContext> processor() {
            Assert.state(JWSAlgorithm.Family.RSA.contains(this.jwsAlgorithm), () -> {
                return "The provided key is of type RSA; however the signature algorithm is of some other type: " + this.jwsAlgorithm + ". Please indicate one of RS256, RS384, or RS512.";
            });
            JWSKeySelector<SecurityContext> jwsKeySelector = new SingleKeyJWSKeySelector(this.jwsAlgorithm, this.key);
            DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor();
            jwtProcessor.setJWSKeySelector(jwsKeySelector);
            jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
            });
            this.jwtProcessorCustomizer.accept(jwtProcessor);
            return jwtProcessor;
        }

        public NimbusJwtDecoder build() {
            return new NimbusJwtDecoder(this.processor());
        }
    }

    public static final class JwkSetUriJwtDecoderBuilder {
        private String jwkSetUri;
        private Set<SignatureAlgorithm> signatureAlgorithms;
        private RestOperations restOperations;
        private Cache cache;
        private Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer;

        private JwkSetUriJwtDecoderBuilder(String jwkSetUri) {
            this.signatureAlgorithms = new HashSet();
            this.restOperations = new RestTemplate();
            Assert.hasText(jwkSetUri, "jwkSetUri cannot be empty");
            this.jwkSetUri = jwkSetUri;
            this.jwtProcessorCustomizer = (processor) -> {
            };
        }

        public JwkSetUriJwtDecoderBuilder jwsAlgorithm(SignatureAlgorithm signatureAlgorithm) {
            Assert.notNull(signatureAlgorithm, "signatureAlgorithm cannot be null");
            this.signatureAlgorithms.add(signatureAlgorithm);
            return this;
        }

        public JwkSetUriJwtDecoderBuilder jwsAlgorithms(Consumer<Set<SignatureAlgorithm>> signatureAlgorithmsConsumer) {
            Assert.notNull(signatureAlgorithmsConsumer, "signatureAlgorithmsConsumer cannot be null");
            signatureAlgorithmsConsumer.accept(this.signatureAlgorithms);
            return this;
        }

        public JwkSetUriJwtDecoderBuilder restOperations(RestOperations restOperations) {
            Assert.notNull(restOperations, "restOperations cannot be null");
            this.restOperations = restOperations;
            return this;
        }

        public JwkSetUriJwtDecoderBuilder cache(Cache cache) {
            Assert.notNull(cache, "cache cannot be null");
            this.cache = cache;
            return this;
        }

        public JwkSetUriJwtDecoderBuilder jwtProcessorCustomizer(Consumer<ConfigurableJWTProcessor<SecurityContext>> jwtProcessorCustomizer) {
            Assert.notNull(jwtProcessorCustomizer, "jwtProcessorCustomizer cannot be null");
            this.jwtProcessorCustomizer = jwtProcessorCustomizer;
            return this;
        }

        JWSKeySelector<SecurityContext> jwsKeySelector(JWKSource<SecurityContext> jwkSource) {
            if (this.signatureAlgorithms.isEmpty()) {
                return new JWSVerificationKeySelector(JWSAlgorithm.RS256, jwkSource);
            } else {
                Set<JWSAlgorithm> jwsAlgorithms = new HashSet();
                Iterator var3 = this.signatureAlgorithms.iterator();

                while(var3.hasNext()) {
                    SignatureAlgorithm signatureAlgorithm = (SignatureAlgorithm)var3.next();
                    JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(signatureAlgorithm.getName());
                    jwsAlgorithms.add(jwsAlgorithm);
                }

                return new JWSVerificationKeySelector(jwsAlgorithms, jwkSource);
            }
        }

        JWKSource<SecurityContext> jwkSource(ResourceRetriever jwkSetRetriever) {
            if (this.cache == null) {
                return new RemoteJWKSet<>(toURL(this.jwkSetUri), jwkSetRetriever);
            } else {
                JWKSetCache jwkSetCache = new SpringJWKSetCache(this.jwkSetUri, this.cache);
                return new RemoteJWKSet(toURL(this.jwkSetUri), jwkSetRetriever, jwkSetCache);
            }
        }

        JWTProcessor<SecurityContext> processor() {
            ResourceRetriever jwkSetRetriever = new RestOperationsResourceRetriever(this.restOperations);
            JWKSource<SecurityContext> jwkSource = this.jwkSource(jwkSetRetriever);
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor();
            jwtProcessor.setJWSKeySelector(this.jwsKeySelector(jwkSource));
            jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
            });
            this.jwtProcessorCustomizer.accept(jwtProcessor);
            return jwtProcessor;
        }

        public NimbusJwtDecoder build() {
            return new NimbusJwtDecoder(this.processor());
        }

        private static URL toURL(String url) {
            try {
                return new URL(url);
            } catch (MalformedURLException var2) {
                throw new IllegalArgumentException("Invalid JWK Set URL \"" + url + "\" : " + var2.getMessage(), var2);
            }
        }

        private static class RestOperationsResourceRetriever implements ResourceRetriever {
            private static final MediaType APPLICATION_JWK_SET_JSON = new MediaType("application", "jwk-set+json");
            private final RestOperations restOperations;

            RestOperationsResourceRetriever(RestOperations restOperations) {
                Assert.notNull(restOperations, "restOperations cannot be null");
                this.restOperations = restOperations;
            }

            public Resource retrieveResource(URL url) throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, APPLICATION_JWK_SET_JSON));
                ResponseEntity<String> response = this.getResponse(url, headers);
                if (response.getStatusCodeValue() != 200) {
                    throw new IOException(response.toString());
                } else {
                    return new Resource((String)response.getBody(), "UTF-8");
                }
            }

            private ResponseEntity<String> getResponse(URL url, HttpHeaders headers) throws IOException {
                try {
                    RequestEntity<Void> request = new RequestEntity(headers, HttpMethod.GET, url.toURI());
                    return this.restOperations.exchange(request, String.class);
                } catch (Exception var4) {
                    throw new IOException(var4);
                }
            }
        }

        private static final class SpringJWKSetCache implements JWKSetCache {
            private final String jwkSetUri;
            private final Cache cache;
            private JWKSet jwkSet;

            SpringJWKSetCache(String jwkSetUri, Cache cache) {
                this.jwkSetUri = jwkSetUri;
                this.cache = cache;
                this.updateJwkSetFromCache();
            }

            private void updateJwkSetFromCache() {
                String cachedJwkSet = (String)this.cache.get(this.jwkSetUri, String.class);
                if (cachedJwkSet != null) {
                    try {
                        this.jwkSet = JWKSet.parse(cachedJwkSet);
                    } catch (java.text.ParseException var3) {
                    }
                }

            }

            public void put(JWKSet jwkSet) {
                this.jwkSet = jwkSet;
                this.cache.put(this.jwkSetUri, jwkSet.toString(false));
            }

            public JWKSet get() {
                return !this.requiresRefresh() ? this.jwkSet : null;
            }

            public boolean requiresRefresh() {
                return this.cache.get(this.jwkSetUri) == null;
            }
        }
    }
}