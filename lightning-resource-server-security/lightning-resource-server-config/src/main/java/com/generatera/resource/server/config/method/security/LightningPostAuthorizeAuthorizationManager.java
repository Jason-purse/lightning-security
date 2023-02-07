package com.generatera.resource.server.config.method.security;

import org.springframework.aop.support.AopUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.MethodInvocationResult;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class LightningPostAuthorizeAuthorizationManager implements AuthorizationManager<MethodInvocationResult> {
    private final LightningPostAuthorizeAuthorizationManager.PostAuthorizeExpressionAttributeRegistry registry = new LightningPostAuthorizeAuthorizationManager.PostAuthorizeExpressionAttributeRegistry();
    private MethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();

    public LightningPostAuthorizeAuthorizationManager() {
    }

    public void setExpressionHandler(MethodSecurityExpressionHandler expressionHandler) {
        Assert.notNull(expressionHandler, "expressionHandler cannot be null");
        this.expressionHandler = expressionHandler;
    }

    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocationResult mi) {
        ExpressionAttribute attribute = this.registry.getAttribute(mi.getMethodInvocation());
        if (attribute == ExpressionAttribute.NULL_ATTRIBUTE) {
            return null;
        } else {
            EvaluationContext ctx = this.expressionHandler.createEvaluationContext(authentication.get(), mi.getMethodInvocation());
            this.expressionHandler.setReturnObject(mi.getResult(), ctx);
            boolean granted = ExpressionUtils.evaluateAsBoolean(attribute.getExpression(), ctx);
            return new ExpressionAttributeAuthorizationDecision(granted, attribute);
        }
    }

    private final class PostAuthorizeExpressionAttributeRegistry extends AbstractExpressionAttributeRegistry<ExpressionAttribute> {
        private PostAuthorizeExpressionAttributeRegistry() {
        }

        @NonNull
        ExpressionAttribute resolveAttribute(Method method, Class<?> targetClass) {
            Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            LightningPostAuthorize postAuthorize = this.findPostAuthorizeAnnotation(specificMethod);
            StringBuilder builder = new StringBuilder();
            LightningPrePostMethodSecurityMetadataSource.handleRolesAndAuthorities(
                    postAuthorize.roles(), postAuthorize.authorities(),
                    builder
            );
            String expressionString = builder.toString();
            return expressionString.length() > 0 ?  new ExpressionAttribute(expressionHandler.getExpressionParser().parseExpression(
                    expressionString)) : ExpressionAttribute.NULL_ATTRIBUTE;
        }

        private LightningPostAuthorize findPostAuthorizeAnnotation(Method method) {
            LightningPostAuthorize postAuthorize = AuthorizationAnnotationUtils.findUniqueAnnotation(method, LightningPostAuthorize.class);
            return postAuthorize != null ? postAuthorize : AuthorizationAnnotationUtils.findUniqueAnnotation(method.getDeclaringClass(), LightningPostAuthorize.class);
        }
    }
}
