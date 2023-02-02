package com.generatera.authorization.application.server.config.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

import java.lang.annotation.Annotation;

public class MyQualifierAnnotationAutowireCandidateResolver extends QualifierAnnotationAutowireCandidateResolver {
    @Override
    public boolean checkQualifier(@NotNull BeanDefinitionHolder bdHolder, @NotNull Annotation annotation, @NotNull TypeConverter typeConverter) {
        return super.checkQualifier(bdHolder, annotation, typeConverter);
    }

}
