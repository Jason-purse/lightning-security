package com.generatera.resource.server.config.method.security;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.access.prepost.PostAuthorize;

import java.lang.annotation.*;

/**
 * 默认填充,通过下面的属性进行 配置 ...
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LightningPostAuthorize {

    public static final String DEFAULT_IDENTIFIER = "default";

    /**
     * 标识符 ..
     */
    @AliasFor("value")
    String identifier() default DEFAULT_IDENTIFIER;

    /**
     * 标识符 ..
     */
    @AliasFor("identifier")
    String value() default DEFAULT_IDENTIFIER;

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


    /**
     *  授权形式(决定了底层的权限判断的语句创建)
     *
     *  如果使用默认这种形式,那么 roles / authorities 没有任何用处 ...
     *
     *  如果填充为null,则
     */
    AuthorizeMode authorizeMode() default AuthorizeMode.ROLE_TO_AUTHORITIES;


    String description() default "";

    /**
     * 如果不填写,则默认根据RequestMapping 来决定是读或者写(但是如果有特殊要求,例如读写,则需要加对应的行为)
     * @see ResourceBehavior
     */
    String behavior() default "";
}
