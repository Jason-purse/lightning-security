package com.generatera.authorization.application.server.config.securityContext;

import com.generatera.authorization.application.server.config.model.entity.LightningSecurityContextEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContext;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 11:20
 * @Description 用来反序列化 Authentication
 *
 * 实现必须保证线程安全 ..
 */
public interface LightningAuthenticationParser {

    @NotNull
    SecurityContext parse(@NotNull LightningSecurityContextEntity securityContext);

    @NotNull
    LightningSecurityContextEntity serialize(@NotNull SecurityContext securityContext);
}
