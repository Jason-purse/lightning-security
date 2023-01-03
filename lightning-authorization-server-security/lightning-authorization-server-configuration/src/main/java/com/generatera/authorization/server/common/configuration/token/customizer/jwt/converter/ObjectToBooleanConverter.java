package com.generatera.authorization.server.common.configuration.token.customizer.jwt.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.Set;

final class ObjectToBooleanConverter implements GenericConverter {
    ObjectToBooleanConverter() {
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, Boolean.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        } else if (source instanceof Boolean) {
            return source;
        } else {
            return source instanceof String ? Boolean.valueOf((String)source) : null;
        }
    }
}
