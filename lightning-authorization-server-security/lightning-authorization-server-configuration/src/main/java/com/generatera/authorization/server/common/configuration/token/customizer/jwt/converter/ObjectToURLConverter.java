package com.generatera.authorization.server.common.configuration.token.customizer.jwt.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

final class ObjectToURLConverter implements GenericConverter {
    ObjectToURLConverter() {
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object.class, URL.class));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        } else if (source instanceof URL) {
            return source;
        } else {
            try {
                return (new URI(source.toString())).toURL();
            } catch (Exception var5) {
                return null;
            }
        }
    }
}
