package com.generatera.authorization.application.server.config.securityContext;

import com.generatera.security.authorization.server.specification.components.authentication.LightningSecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 9:50
 * @Description 保存 SecurityContext 仓库
 *
 * stateless security Context 仓库 ...
 */
public class DefaultSecurityContextRepository implements LightningSecurityContextRepository {

    @Override
    public SecurityContext internalLoadContext(HttpServletRequest request) {
        return SecurityContextHolder.createEmptyContext();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        // 永远不保存 ...
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }
}
