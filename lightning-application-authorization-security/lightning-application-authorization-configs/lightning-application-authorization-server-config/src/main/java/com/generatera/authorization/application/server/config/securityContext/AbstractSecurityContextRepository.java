package com.generatera.authorization.application.server.config.securityContext;

import com.generatera.authorization.server.common.configuration.token.LightningAuthenticationToken;
import com.generatera.authorization.server.common.configuration.token.LightningSecurityContextRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 10:44
 * @Description 抽象的 安全上下文仓库 ...
 */
public abstract class AbstractSecurityContextRepository implements LightningSecurityContextRepository {

    protected Optional<String> parseToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(TOKEN_HEADER));
    }

    protected Optional<LightningAuthenticationToken> acquireAuthenticationToken(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        return Optional.ofNullable(request.getAttribute(LightningAuthenticationToken.TOKEN_REQUEST_ATTRIBUTE)).map(ele -> ((LightningAuthenticationToken) ele));
    }

    @Override
    public SecurityContext internalLoadContext(HttpServletRequest request) {
        return parseToken(request).map(this::doLoadContext).orElse(null);
    }

    @Nullable
    protected abstract SecurityContext doLoadContext(String token);

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        Optional<LightningAuthenticationToken> lightningAuthenticationToken = acquireAuthenticationToken(context, request, response);
        Assert.isTrue(lightningAuthenticationToken.isPresent(),"lightning authentication token must not be null !!!");
        doSaveContext(context,lightningAuthenticationToken.get());
    }

    protected abstract void doSaveContext(SecurityContext securityContext,LightningAuthenticationToken authenticationToken);

    @Override
    public boolean containsContext(HttpServletRequest request) {
        // 解析token 如果不存在直接返回 ..
        return parseToken(request)
                .map(this::doLoadContext)
                .isPresent();
    }
}
