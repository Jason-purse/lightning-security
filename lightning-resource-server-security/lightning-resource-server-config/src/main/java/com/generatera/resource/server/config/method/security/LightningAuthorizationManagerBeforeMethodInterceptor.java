package com.generatera.resource.server.config.method.security;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.core.Ordered;
import org.springframework.core.log.LogMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.function.Supplier;
/**
 * @author FLJ
 * @date 2023/2/6
 * @time 16:10
 * @Description 扫描 权限和权限点的 方法拦截器
 */
public class LightningAuthorizationManagerBeforeMethodInterceptor implements Ordered, MethodInterceptor, PointcutAdvisor, AopInfrastructureBean {

    static final Supplier<Authentication> AUTHENTICATION_SUPPLIER = () -> {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("An Authentication object was not found in the SecurityContext");
        } else {
            return authentication;
        }
    };
    private final Log logger = LogFactory.getLog(this.getClass());
    private final Pointcut pointcut;
    private final AuthorizationManager<MethodInvocation> authorizationManager;
    private int order;
    private AuthorizationEventPublisher eventPublisher;

    public LightningAuthorizationManagerBeforeMethodInterceptor(Pointcut pointcut, AuthorizationManager<MethodInvocation> authorizationManager) {
        this.order = AuthorizationInterceptorsOrder.FIRST.getOrder();
        this.eventPublisher = LightningAuthorizationManagerBeforeMethodInterceptor::noPublish;
        Assert.notNull(pointcut, "pointcut cannot be null");
        Assert.notNull(authorizationManager, "authorizationManager cannot be null");
        this.pointcut = pointcut;
        this.authorizationManager = authorizationManager;
    }

    public static LightningAuthorizationManagerBeforeMethodInterceptor preAuthorize() {
        return preAuthorize(new LightningPreAuthorizeAuthorizationManager());
    }

    public static LightningAuthorizationManagerBeforeMethodInterceptor preAuthorize(LightningPreAuthorizeAuthorizationManager authorizationManager) {
        LightningAuthorizationManagerBeforeMethodInterceptor interceptor = new LightningAuthorizationManagerBeforeMethodInterceptor(AuthorizationMethodPointcuts.forAnnotations(LightningPreAuthorize.class), authorizationManager);
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() - 1);
        return interceptor;
    }


    public Object invoke(MethodInvocation mi) throws Throwable {
        this.attemptAuthorization(mi);
        return mi.proceed();
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setAuthorizationEventPublisher(AuthorizationEventPublisher eventPublisher) {
        Assert.notNull(eventPublisher, "eventPublisher cannot be null");
        this.eventPublisher = eventPublisher;
    }

    public Pointcut getPointcut() {
        return this.pointcut;
    }

    public Advice getAdvice() {
        return this;
    }

    public boolean isPerInstance() {
        return true;
    }

    private void attemptAuthorization(MethodInvocation mi) {
        this.logger.debug(LogMessage.of(() -> {
            return "Authorizing method invocation " + mi;
        }));
        AuthorizationDecision decision = this.authorizationManager.check(AUTHENTICATION_SUPPLIER, mi);
        this.eventPublisher.publishAuthorizationEvent(AUTHENTICATION_SUPPLIER, mi, decision);
        if (decision != null && !decision.isGranted()) {
            this.logger.debug(LogMessage.of(() -> {
                return "Failed to authorize " + mi + " with authorization manager " + this.authorizationManager + " and decision " + decision;
            }));
            throw new AccessDeniedException("Access Denied");
        } else {
            this.logger.debug(LogMessage.of(() -> {
                return "Authorized method invocation " + mi;
            }));
        }
    }

    private static <T> void noPublish(Supplier<Authentication> authentication, T object, AuthorizationDecision decision) {
    }
}
