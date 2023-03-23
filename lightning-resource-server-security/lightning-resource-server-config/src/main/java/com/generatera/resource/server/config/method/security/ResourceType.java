package com.generatera.resource.server.config.method.security;
/**
 * @author FLJ
 * @date 2023/3/14
 * @time 10:42
 * @Description 资源类型
 */
public interface ResourceType {

    public String getType();
    public static final ResourceType BACKEND_TYPE = new ResourceType() {
        @Override
        public String getType() {
            return "backend";
        }
    };

    public static final ResourceType FRONT_TYPE = new ResourceType() {
        @Override
        public String getType() {
            return "frontend";
        }
    };

    // 可以继续扩展
}
