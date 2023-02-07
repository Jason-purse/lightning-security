package com.generatera.resource.server.config.method.security;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 自定义表达式 注解来支持方法安全 ...
 *
 * 可以使用关键字,roles,以及 authorities 来获取注解中配置的角色信息,权限点信息 ..
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LightningPreAuthorize {

    /**
     * 标识符 ..
     */
    @AliasFor("value")
    String identifier() default "";

    /**
     * 标识符 ..
     */
    @AliasFor("identifier")
    String value() default "";

    /**
     * 需要的角色(角色是或者的关系)
     * 自动添加 角色前缀 ...
     */
    String[] roles() default {};


    /**
     * 需要的权限点(是and的关系) ...
     */
    String[] authorities() default {};
}
