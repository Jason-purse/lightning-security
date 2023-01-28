package com.generatera.authorization.server.common.configuration.provider.metadata;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * @author FLJ
 * @date 2023/1/28
 * @time 12:48
 * @Description 根据包含的claim 以及它的对应类型转换器来可选的转换对应的claim ...
 */
public final class ClaimTypeConverter implements Converter<Map<String, Object>, Map<String, Object>> {
    private final Map<String, Converter<Object, ?>> claimTypeConverters;

    public ClaimTypeConverter(Map<String, Converter<Object, ?>> claimTypeConverters) {
        Assert.notEmpty(claimTypeConverters, "claimTypeConverters cannot be empty");
        Assert.noNullElements(claimTypeConverters.values().toArray(), "Converter(s) cannot be null");
        this.claimTypeConverters = Collections.unmodifiableMap(new LinkedHashMap<>(claimTypeConverters));
    }

    public Map<String, Object> convert(@NotNull Map<String, Object> claims) {
        if (CollectionUtils.isEmpty(claims)) {
            return claims;
        } else {
            Map<String, Object> result = new HashMap<>(claims);
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