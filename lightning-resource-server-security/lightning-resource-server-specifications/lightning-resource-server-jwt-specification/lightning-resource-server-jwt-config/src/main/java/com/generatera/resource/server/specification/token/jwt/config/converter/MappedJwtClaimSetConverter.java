package com.generatera.resource.server.specification.token.jwt.config.converter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class MappedJwtClaimSetConverter implements Converter<Map<String, Object>, Map<String, Object>> {
    private static final ConversionService CONVERSION_SERVICE = ClaimConversionService.getSharedInstance();
    private static final TypeDescriptor OBJECT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Object.class);
    private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);
    private static final TypeDescriptor INSTANT_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Instant.class);
    private static final TypeDescriptor URL_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(URL.class);
    private final Map<String, Converter<Object, ?>> claimTypeConverters;

    public MappedJwtClaimSetConverter(Map<String, Converter<Object, ?>> claimTypeConverters) {
        Assert.notNull(claimTypeConverters, "claimTypeConverters cannot be null");
        this.claimTypeConverters = claimTypeConverters;
    }

    public static MappedJwtClaimSetConverter withDefaults(Map<String, Converter<Object, ?>> claimTypeConverters) {
        Assert.notNull(claimTypeConverters, "claimTypeConverters cannot be null");
        Converter<Object, ?> stringConverter = getConverter(STRING_TYPE_DESCRIPTOR);
        Converter<Object, ?> collectionStringConverter = getConverter(TypeDescriptor.collection(Collection.class, STRING_TYPE_DESCRIPTOR));
        Map<String, Converter<Object, ?>> claimNameToConverter = new HashMap();
        claimNameToConverter.put("aud", collectionStringConverter);
        claimNameToConverter.put("exp", MappedJwtClaimSetConverter::convertInstant);
        claimNameToConverter.put("iat", MappedJwtClaimSetConverter::convertInstant);
        claimNameToConverter.put("iss", MappedJwtClaimSetConverter::convertIssuer);
        claimNameToConverter.put("jti", stringConverter);
        claimNameToConverter.put("nbf", MappedJwtClaimSetConverter::convertInstant);
        claimNameToConverter.put("sub", stringConverter);
        claimNameToConverter.putAll(claimTypeConverters);
        return new MappedJwtClaimSetConverter(claimNameToConverter);
    }

    private static Converter<Object, ?> getConverter(TypeDescriptor targetDescriptor) {
        return (source) -> {
            return CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, targetDescriptor);
        };
    }

    private static Instant convertInstant(Object source) {
        if (source == null) {
            return null;
        } else {
            Instant result = (Instant)CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, INSTANT_TYPE_DESCRIPTOR);
            Assert.state(result != null, () -> {
                return "Could not coerce " + source + " into an Instant";
            });
            return result;
        }
    }

    private static String convertIssuer(Object source) {
        if (source == null) {
            return null;
        } else {
            URL result = (URL)CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, URL_TYPE_DESCRIPTOR);
            if (result != null) {
                return result.toExternalForm();
            } else if (source instanceof String && ((String)source).contains(":")) {
                try {
                    return (new URI((String)source)).toString();
                } catch (Exception var3) {
                    throw new IllegalStateException("Could not coerce " + source + " into a URI String", var3);
                }
            } else {
                return (String)CONVERSION_SERVICE.convert(source, OBJECT_TYPE_DESCRIPTOR, STRING_TYPE_DESCRIPTOR);
            }
        }
    }

    public Map<String, Object> convert(Map<String, Object> claims) {
        Assert.notNull(claims, "claims cannot be null");
        Map<String, Object> mappedClaims = new HashMap(claims);
        Iterator var3 = this.claimTypeConverters.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, Converter<Object, ?>> entry = (Map.Entry)var3.next();
            String claimName = (String)entry.getKey();
            Converter<Object, ?> converter = (Converter)entry.getValue();
            if (converter != null) {
                Object claim = claims.get(claimName);
                Object mappedClaim = converter.convert(claim);
                mappedClaims.compute(claimName, (key, value) -> {
                    return mappedClaim;
                });
            }
        }

        Instant issuedAt = (Instant)mappedClaims.get("iat");
        Instant expiresAt = (Instant)mappedClaims.get("exp");
        if (issuedAt == null && expiresAt != null) {
            mappedClaims.put("iat", expiresAt.minusSeconds(1L));
        }

        return mappedClaims;
    }
}
