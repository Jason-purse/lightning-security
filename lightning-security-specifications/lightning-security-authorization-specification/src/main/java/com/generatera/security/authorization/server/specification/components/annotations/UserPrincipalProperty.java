package com.generatera.security.authorization.server.specification.components.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 能够被类继承 !!!
 *
 * 主要作用就是在controller上进行 {@link com.generatera.security.authorization.server.specification.LightningUserPrincipal}
 * 信息的注入 !!!!
 * 如果无法转换,则是空 !!!!
 *
 * 只能对字段进行注入 !!!
 *
 * 能够基于普通参数进行属性注入 ...
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Inherited
public @interface UserPrincipalProperty {
    /**
     * 默认为空,根据 目标字段 / 或者setter方法的java bean 规范规则来获取 名称 ..
     */
    @AliasFor("name")
    String value() default "";
    /**
     * 默认为空,根据 目标字段 / 或者setter方法的java bean 规范规则来获取 名称 ..
     */
    @AliasFor("value")
    String name() default "";


    /**
     * 可选覆盖 ..
     */
    boolean optional() default false;
}
