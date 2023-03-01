package com.generatera.security.authorization.server.specification.components.annotations;

import java.lang.annotation.*;
/**
 * @author FLJ
 * @date 2023/3/1
 * @time 13:51
 * @Description 支持 请求header 解析 !!
 *
 * 进行header 解析标记 !!! 减少参数反射属性注入的范围 !!!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.PARAMETER})
@Inherited
@Documented
public @interface RequestHeaderInject {

}
