package com.generatera.resource.server.config.method.security;
/**
 * @author FLJ
 * @date 2023/3/23
 * @time 10:36
 * @Description 资源阶段
 */
public interface ResourceInvokeEvaluatePhase {

    public String getPhase();

    public static final ResourceInvokeEvaluatePhase PRE_INVOKE = new ResourceInvokeEvaluatePhase() {
        @Override
        public String getPhase() {
            return "pre";
        }
    };

    public static final ResourceInvokeEvaluatePhase POST_INVOKE = new ResourceInvokeEvaluatePhase() {
        @Override
        public String getPhase() {
            return "post";
        }
    };
}
