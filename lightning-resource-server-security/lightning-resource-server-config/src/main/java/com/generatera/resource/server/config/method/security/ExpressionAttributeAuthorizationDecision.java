package com.generatera.resource.server.config.method.security;

import org.springframework.security.authorization.AuthorizationDecision;
/**
 * @author FLJ
 * @date 2023/2/6
 * @time 16:33
 * @Description 覆盖 ...
 */
public class ExpressionAttributeAuthorizationDecision extends AuthorizationDecision {
    private final ExpressionAttribute expressionAttribute;

    public ExpressionAttributeAuthorizationDecision(boolean granted, ExpressionAttribute expressionAttribute) {
        super(granted);
        this.expressionAttribute = expressionAttribute;
    }

    public ExpressionAttribute getExpressionAttribute() {
        return this.expressionAttribute;
    }

    public String toString() {
        return this.getClass().getSimpleName() + " [granted=" + this.isGranted() + ", expressionAttribute=" + this.expressionAttribute + ']';
    }
}