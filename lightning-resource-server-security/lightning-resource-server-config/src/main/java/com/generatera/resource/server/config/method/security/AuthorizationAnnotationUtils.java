package com.generatera.resource.server.config.method.security;

import org.springframework.core.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;

final class AuthorizationAnnotationUtils {
    static <A extends Annotation> A findUniqueAnnotation(Method method, Class<A> annotationType) {
        MergedAnnotations mergedAnnotations = MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none());
        if (hasDuplicate(mergedAnnotations, annotationType)) {
            throw new AnnotationConfigurationException("Found more than one annotation of type " + annotationType + " attributed to " + method + " Please remove the duplicate annotations and publish a bean to handle your authorization logic.");
        } else {
            return AnnotationUtils.findAnnotation(method, annotationType);
        }
    }

    static <A extends Annotation> A findUniqueAnnotation(Class<?> type, Class<A> annotationType) {
        MergedAnnotations mergedAnnotations = MergedAnnotations.from(type, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none());
        if (hasDuplicate(mergedAnnotations, annotationType)) {
            throw new AnnotationConfigurationException("Found more than one annotation of type " + annotationType + " attributed to " + type + " Please remove the duplicate annotations and publish a bean to handle your authorization logic.");
        } else {
            return AnnotationUtils.findAnnotation(type, annotationType);
        }
    }

    private static <A extends Annotation> boolean hasDuplicate(MergedAnnotations mergedAnnotations, Class<A> annotationType) {
        boolean alreadyFound = false;
        Iterator var3 = mergedAnnotations.iterator();

        while(var3.hasNext()) {
            MergedAnnotation<Annotation> mergedAnnotation = (MergedAnnotation)var3.next();
            if (mergedAnnotation.getType() == annotationType) {
                if (alreadyFound) {
                    return true;
                }

                alreadyFound = true;
            }
        }

        return false;
    }

    private AuthorizationAnnotationUtils() {
    }
}