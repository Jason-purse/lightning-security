package com.generatera.resource.server.config.method.security;

import com.jianyue.lightning.boot.starter.util.dataflow.Tuple4;

public interface LightningInvocationAttribute {
    /**
     * 获取方法标识符 资源类型, 以及动作阶段(pre / post) 以及动作类型(read / write / read_write)
     * @return 方法标识符字符串表现形式
     */
    Tuple4<String,String,String,String> getMethodIdentifierWithActionAndType();
}
