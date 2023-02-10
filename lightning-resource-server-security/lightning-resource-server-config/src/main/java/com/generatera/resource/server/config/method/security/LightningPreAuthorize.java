package com.generatera.resource.server.config.method.security;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 自定义表达式 注解来支持方法安全 ...
 * <p>
 * 可以使用关键字,roles,以及 authorities 来获取注解中配置的角色信息,权限点信息 ..
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LightningPreAuthorize {

    public static final String DEFAULT_IDENTIFIER = "default";

    /**
     * 标识符 ..  简短名称 ...
     */
    @AliasFor("value")
    String identifier() default DEFAULT_IDENTIFIER;

    /**
     * 标识符 ..
     */
    @AliasFor("identifier")
    String value() default DEFAULT_IDENTIFIER;

    /**
     * 需要的角色
     * 正常来说,访问这个资源,可能需要会有多个角色 控制 ..
     * 支持基于权限点的角色扩展控制 .. ...
     *
     */
    String[] roles() default {};


    /**
     * 需要的权限点信息 正常来说,访问这个资源,只需要一个权限点进行标识 ..
     *
     * 给出列表是为了好扩展 ...
     */
    String[] authorities() default {};


    /**
     * 描述信息 ...
     */
    String description() default "";
}
