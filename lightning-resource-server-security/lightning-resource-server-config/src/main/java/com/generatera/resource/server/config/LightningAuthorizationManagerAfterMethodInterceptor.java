package com.generatera.resource.server.config;

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
import org.springframework.security.authorization.method.MethodInvocationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import java.util.function.Supplier;

public class LightningAuthorizationManagerAfterMethodInterceptor implements Ordered, MethodInterceptor, PointcutAdvisor, AopInfrastructureBean {
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
    private final AuthorizationManager<MethodInvocationResult> authorizationManager;
    private int order;
    private AuthorizationEventPublisher eventPublisher = LightningAuthorizationManagerAfterMethodInterceptor::noPublish;

    public LightningAuthorizationManagerAfterMethodInterceptor(Pointcut pointcut, AuthorizationManager<MethodInvocationResult> authorizationManager) {
        Assert.notNull(pointcut, "pointcut cannot be null");
        Assert.notNull(authorizationManager, "authorizationManager cannot be null");
        this.pointcut = pointcut;
        this.authorizationManager = authorizationManager;
    }

    public static LightningAuthorizationManagerAfterMethodInterceptor postAuthorize() {
        return postAuthorize(new LightningPostAuthorizeAuthorizationManager());
    }

    public static LightningAuthorizationManagerAfterMethodInterceptor postAuthorize(LightningPostAuthorizeAuthorizationManager authorizationManager) {
        LightningAuthorizationManagerAfterMethodInterceptor interceptor = new LightningAuthorizationManagerAfterMethodInterceptor(AuthorizationMethodPointcuts.forAnnotations(LightningPostAuthorize.class), authorizationManager);
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() - 1);
        return interceptor;
    }

    public Object invoke(MethodInvocation mi) throws Throwable {
        Object result = mi.proceed();
        this.attemptAuthorization(mi, result);
        return result;
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

    private void attemptAuthorization(MethodInvocation mi, Object result) {
        this.logger.debug(LogMessage.of(() -> {
            return "Authorizing method invocation " + mi;
        }));
        MethodInvocationResult object = new MethodInvocationResult(mi, result);
        AuthorizationDecision decision = this.authorizationManager.check(AUTHENTICATION_SUPPLIER, object);
        this.eventPublisher.publishAuthorizationEvent(AUTHENTICATION_SUPPLIER, object, decision);
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
