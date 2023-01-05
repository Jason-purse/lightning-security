package com.generatera.resource.server.specification.token.jwt.config.converter;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.ClassUtils;

import java.util.*;

final class ObjectToListStringConverter implements ConditionalGenericConverter {
    ObjectToListStringConverter() {
    }

    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertibleTypes = new LinkedHashSet();
        convertibleTypes.add(new ConvertiblePair(Object.class, List.class));
        convertibleTypes.add(new ConvertiblePair(Object.class, Collection.class));
        return convertibleTypes;
    }

    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return targetType.getElementTypeDescriptor() == null || targetType.getElementTypeDescriptor().getType().equals(String.class) || sourceType == null || ClassUtils.isAssignable(sourceType.getType(), targetType.getElementTypeDescriptor().getType());
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        } else if (source instanceof Collection) {
            Collection<String> results = new ArrayList();
            Iterator var5 = ((Collection)source).iterator();

            while(var5.hasNext()) {
                Object object = var5.next();
                if (object != null) {
                    results.add(object.toString());
                }
            }

            return results;
        } else {
            return Collections.singletonList(source.toString());
        }
    }
}
