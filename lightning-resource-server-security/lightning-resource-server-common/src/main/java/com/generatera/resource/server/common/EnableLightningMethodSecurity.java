package com.generatera.resource.server.common;

import java.lang.annotation.*;

/**
 * 注解处理器,会寻找注解了这个注释的类 ..
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface EnableLightningMethodSecurity {

    String value() default "module_name";
}
