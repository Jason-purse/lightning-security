package com.generatera.authorization.ext.oauth2.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在oauth2中需要进行序列化的 class 必须增加此注解,加入序列化白名单 ..
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
@JacksonAnnotation
public @interface SafeSerialize {

}
