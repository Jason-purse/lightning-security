package com.generatera.security.authorization.server.specification.components.annotations;

import org.springframework.core.annotation.AliasFor;

/**
 * @author FLJ
 * @date 2023/3/1
 * @time 15:32
 * @Description 请求头参数 解析
 */
public @interface RequestHeaderArgument {

    @AliasFor("name")
    String value();

    @AliasFor("value")
    String name();
}
