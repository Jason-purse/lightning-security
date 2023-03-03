package com.generatera.security.authorization.server.specification.components.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 15:32
 * @Description 请求头参数 解析
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.PARAMETER})
@Inherited
@Documented
public @interface RequestHeaderArgument {
    public static final String DEFAULT_REQUEST_HEADER_NAME = "header-argument";

    @AliasFor("name")
    String value() default DEFAULT_REQUEST_HEADER_NAME;

    @AliasFor("value")
    String name() default DEFAULT_REQUEST_HEADER_NAME;
}
