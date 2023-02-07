package com.generatera.resource.server.config.method.security;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import java.lang.annotation.Annotation;

final class AuthorizationMethodPointcuts {
    @SafeVarargs
    static Pointcut forAnnotations(Class<? extends Annotation>... annotations) {
        ComposablePointcut pointcut = null;
        Class[] var2 = annotations;
        int var3 = annotations.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Class<? extends Annotation> annotation = var2[var4];
            if (pointcut == null) {
                pointcut = new ComposablePointcut(classOrMethod(annotation));
            } else {
                pointcut.union(classOrMethod(annotation));
            }
        }

        return pointcut;
    }

    private static Pointcut classOrMethod(Class<? extends Annotation> annotation) {
        return Pointcuts.union(new AnnotationMatchingPointcut((Class)null, annotation, true), new AnnotationMatchingPointcut(annotation, true));
    }

    private AuthorizationMethodPointcuts() {
    }
}