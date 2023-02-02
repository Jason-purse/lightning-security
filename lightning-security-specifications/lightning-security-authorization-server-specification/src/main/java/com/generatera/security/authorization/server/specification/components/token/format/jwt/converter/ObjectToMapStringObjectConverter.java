package com.generatera.security.authorization.server.specification.components.token.format.jwt.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class ObjectToMapStringObjectConverter implements ConditionalGenericConverter {
    ObjectToMapStringObjectConverter() {
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, Map.class));
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.getElementTypeDescriptor() == null || targetType.getMapKeyTypeDescriptor().getType().equals(String.class);
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        } else if (!(source instanceof Map)) {
            return null;
        } else {
            Map<?, ?> sourceMap = (Map)source;
            Map<String, Object> result = new HashMap();
            sourceMap.forEach((k, v) -> {
                result.put(k.toString(), v);
            });
            return result;
        }
    }
}
