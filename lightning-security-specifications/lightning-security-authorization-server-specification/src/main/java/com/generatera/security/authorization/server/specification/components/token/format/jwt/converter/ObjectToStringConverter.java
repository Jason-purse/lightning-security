package com.generatera.security.authorization.server.specification.components.token.format.jwt.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.Set;

final class ObjectToStringConverter implements GenericConverter {
    ObjectToStringConverter() {
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, String.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        return source != null ? source.toString() : null;
    }
}