package com.generatera.resource.server.config.method.security;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 默认填充,通过下面的属性进行 配置 ...
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LightningPostAuthorize {

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
     *
     */
    String[] authorities() default {};
}
