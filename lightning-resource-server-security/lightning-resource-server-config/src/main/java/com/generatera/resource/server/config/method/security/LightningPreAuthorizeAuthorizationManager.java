package com.generatera.resource.server.config.method.security;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class LightningPreAuthorizeAuthorizationManager implements AuthorizationManager<MethodInvocation> {
    private final LightningPreAuthorizeAuthorizationManager.PreAuthorizeExpressionAttributeRegistry registry = new LightningPreAuthorizeAuthorizationManager.PreAuthorizeExpressionAttributeRegistry();
    private MethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();

    public LightningPreAuthorizeAuthorizationManager() {
    }

    public void setExpressionHandler(MethodSecurityExpressionHandler expressionHandler) {
        Assert.notNull(expressionHandler, "expressionHandler cannot be null");
        this.expressionHandler = expressionHandler;
    }

    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation mi) {
        ExpressionAttribute attribute = this.registry.getAttribute(mi);
        if (attribute == ExpressionAttribute.NULL_ATTRIBUTE) {
            return null;
        } else {
            EvaluationContext ctx = this.expressionHandler.createEvaluationContext(authentication.get(), mi);
            boolean granted = ExpressionUtils.evaluateAsBoolean(attribute.getExpression(), ctx);
            return new ExpressionAttributeAuthorizationDecision(granted, new ExpressionAttribute(attribute.getExpression()));
        }
    }

    private final class PreAuthorizeExpressionAttributeRegistry extends AbstractExpressionAttributeRegistry<ExpressionAttribute> {
        private PreAuthorizeExpressionAttributeRegistry() {
        }

        @NonNull
        ExpressionAttribute resolveAttribute(Method method, Class<?> targetClass) {
            Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            LightningPreAuthorize preAuthorize = this.findPreAuthorizeAnnotation(specificMethod);
            StringBuilder builder = new StringBuilder();
            LightningPrePostMethodSecurityMetadataSource.handleRolesAndAuthorities(
                    preAuthorize.roles(), preAuthorize.authorities(),builder
            );
            String expressionString = builder.toString();

            return expressionString.length() > 0 ? new ExpressionAttribute(expressionHandler.getExpressionParser().parseExpression(expressionString))
                    : ExpressionAttribute.NULL_ATTRIBUTE;
        }

        private LightningPreAuthorize findPreAuthorizeAnnotation(Method method) {
            LightningPreAuthorize preAuthorize = AuthorizationAnnotationUtils.findUniqueAnnotation(method, LightningPreAuthorize.class);
            return preAuthorize != null ? preAuthorize : (LightningPreAuthorize)AuthorizationAnnotationUtils.findUniqueAnnotation(method.getDeclaringClass(), PreAuthorize.class);
        }
    }


}
