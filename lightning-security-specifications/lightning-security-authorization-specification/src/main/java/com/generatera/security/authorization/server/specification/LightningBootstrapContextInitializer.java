package com.generatera.security.authorization.server.specification;
/**
 * @author FLJ
 * @date 2023/3/10
 * @time 16:54
 * @Description 初始化boot 上下文 ...
 */
public interface LightningBootstrapContextInitializer {

    void initialize(BootstrapContext bootstrapContext);
}
