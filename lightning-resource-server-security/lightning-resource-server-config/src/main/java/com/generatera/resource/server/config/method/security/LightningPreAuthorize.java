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

    // -------------------------- 如果资源是基于角色控制的,那么可以在资源进行角色控制,实现
    // 也就是这里有两种校验格式,例如 通过角色和 一堆权限点 ...
    // 一个资源可以被多个角色访问 , 或者一个资源可以被多个权限点访问 ...

    // 取决于 授权框架是如何使用的, 如果 以角色一对多 多个资源(且此资源是唯一标识的,那么此注解已经足够) ..
    // 如果是一个资源 一对多  多个角色 或者权限点,那么 粒度更小(可以临时决定此资源是否能被给定的角色或者权限点访问,即使它是一个jwt token令牌) ..
    // 我们也能够限制那些角色 可以禁止访问此资源 ..
    // 而不必改变用户和角色之间的关系(因为 Jwt token的情况下,一次性令牌,所含角色信息是固定的) ...

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
     *  授权形式(决定了底层的权限判断的语句创建)
     */
    AuthorizeMode authorizeMode() default AuthorizeMode.ROLE_TO_AUTHORITIES;

    /**
     * 描述信息 ...
     */
    String description() default "";

    /**
     * 如果不填写,则默认根据RequestMapping 来决定是读或者写(但是如果有特殊要求,例如读写,则需要加对应的行为)
     * @see ResourceBehavior
     */
    String behavior() default "";
}
