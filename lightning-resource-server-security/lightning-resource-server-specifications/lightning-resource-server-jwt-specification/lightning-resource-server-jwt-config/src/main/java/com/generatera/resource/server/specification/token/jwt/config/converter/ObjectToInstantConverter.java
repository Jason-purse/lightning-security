package com.generatera.resource.server.specification.token.jwt.config.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

final class ObjectToInstantConverter implements GenericConverter {
    ObjectToInstantConverter() {
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, Instant.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        } else if (source instanceof Instant) {
            return source;
        } else if (source instanceof Date) {
            return ((Date)source).toInstant();
        } else if (source instanceof Number) {
            return Instant.ofEpochSecond(((Number)source).longValue());
        } else {
            try {
                return Instant.ofEpochSecond(Long.parseLong(source.toString()));
            } catch (Exception var6) {
                try {
                    return Instant.parse(source.toString());
                } catch (Exception var5) {
                    return null;
                }
            }
        }
    }
}
