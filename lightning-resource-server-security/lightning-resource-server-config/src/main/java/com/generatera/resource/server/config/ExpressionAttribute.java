package com.generatera.resource.server.config;

import org.springframework.expression.Expression;

class ExpressionAttribute {
        static final ExpressionAttribute NULL_ATTRIBUTE = new ExpressionAttribute(null);
        private final Expression expression;

        ExpressionAttribute(Expression expression) {
            this.expression = expression;
        }

        Expression getExpression() {
            return this.expression;
        }

        public String toString() {
            return this.getClass().getSimpleName() + " [Expression=" + (this.expression != null ? this.expression.getExpressionString() : null) + "]";
        }
    }