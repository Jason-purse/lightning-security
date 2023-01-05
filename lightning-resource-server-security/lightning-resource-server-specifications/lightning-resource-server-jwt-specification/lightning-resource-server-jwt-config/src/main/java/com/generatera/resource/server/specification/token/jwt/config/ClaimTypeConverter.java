package com.generatera.resource.server.specification.token.jwt.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClaimTypeConverter implements Converter<Map<String, Object>, Map<String, Object>> {
    private final Map<String, Converter<Object, ?>> claimTypeConverters;

    public ClaimTypeConverter(Map<String, Converter<Object, ?>> claimTypeConverters) {
        Assert.notEmpty(claimTypeConverters, "claimTypeConverters cannot be empty");
        Assert.noNullElements(claimTypeConverters.values().toArray(), "Converter(s) cannot be null");
        this.claimTypeConverters = Collections.unmodifiableMap(new LinkedHashMap(claimTypeConverters));
    }

    public Map<String, Object> convert(Map<String, Object> claims) {
        if (CollectionUtils.isEmpty(claims)) {
            return claims;
        } else {
            Map<String, Object> result = new HashMap(claims);
            this.claimTypeConverters.forEach((claimName, typeConverter) -> {
                if (claims.containsKey(claimName)) {
                    Object claim = claims.get(claimName);
                    Object mappedClaim = typeConverter.convert(claim);
                    if (mappedClaim != null) {
                        result.put(claimName, mappedClaim);
                    }
                }

            });
            return result;
        }
    }
}