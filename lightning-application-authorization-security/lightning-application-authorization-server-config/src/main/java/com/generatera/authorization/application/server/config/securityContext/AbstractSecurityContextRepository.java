//package com.generatera.authorization.application.server.com.generatera.oauth2.resource.server.config.securityContext;
//
//import com.generatera.security.application.authorization.server.token.specification.LightningApplicationLevelAuthenticationToken;
//import com.generatera.security.authorization.server.specification.components.authentication.LightningSecurityContextRepository;
//import org.jetbrains.annotations.Nullable;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.util.Assert;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Optional;
///**
// * @author FLJ
// * @date 2023/1/5
// * @time 10:44
// * @Description 抽象的 安全上下文仓库 ...
// */
//public abstract class AbstractSecurityContextRepository implements LightningSecurityContextRepository {
//
//    protected Optional<String> parseToken(HttpServletRequest request) {
//        return Optional.ofNullable(request.getHeader(TOKEN_HEADER));
//    }
//
//    protected Optional<LightningApplicationLevelAuthenticationToken> acquireAuthenticationToken(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
//        return Optional.ofNullable(request.getAttribute(LightningApplicationLevelAuthenticationToken.TOKEN_REQUEST_ATTRIBUTE)).map(ele -> ((LightningApplicationLevelAuthenticationToken) ele));
//    }
//
//    @Override
//    public SecurityContext internalLoadContext(HttpServletRequest request) {
//        return parseToken(request).map(this::doLoadContext).orElse(null);
//    }
//
//    @Nullable
//    protected abstract SecurityContext doLoadContext(String token);
//
//    @Override
//    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
//        Optional<LightningApplicationLevelAuthenticationToken> lightningAuthenticationToken = acquireAuthenticationToken(context, request, response);
//        Assert.isTrue(lightningAuthenticationToken.isPresent(),"lightning authentication token must not be null !!!");
//        doSaveContext(context,lightningAuthenticationToken.get());
//    }
//
//    protected abstract void doSaveContext(SecurityContext securityContext,LightningApplicationLevelAuthenticationToken authenticationToken);
//
//    @Override
//    public boolean containsContext(HttpServletRequest request) {
//        // 解析token 如果不存在直接返回 ..
//        return parseToken(request)
//                .map(this::doLoadContext)
//                .isPresent();
//    }
//}
