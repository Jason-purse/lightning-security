package com.generatera.security.authorization.server.specification.components.annotations;

import com.generatera.security.authorization.server.specification.LightningUserContext;

import java.lang.annotation.*;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 13:44
 * @Description {@link com.generatera.security.authorization.server.specification.LightningUserPrincipal 用户属性注入}
 *
 * 减少{@link LightningUserContext#get()}的硬编码 !!!!
 *
 * user principal property inject
 *
 * 是一个标记接口,只有注释了这样的类才会被考虑进行 LightningUserPrincipal 属性 注入 ..
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.PARAMETER,ElementType.FIELD})
@Inherited
@Documented
public @interface UserPrincipalInject {
}
