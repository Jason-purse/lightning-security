package com.generatera.security.authorization.server.specification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 17:30
 * @Description 根据Context抽象获取 引导上下文中的内容 ...
 *
 * 在容器刷新完成之后,清空上下文!!!
 * 所以不要强依赖它 !!!
 * @see LightningSecurityAuthorizationSpecificationAutoConfiguration
 */
public final class BootstrapContext implements Context {

    private final Map<Object, Object> context = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <V> V get(Object key) {
        return (V) context.get(key);
    }

    public boolean hasKey(Object key) {
        return context.containsKey(key);
    }

    public void put(Object key, Object value) {
        context.put(key, value);
    }

    public void clear() {
        this.context.clear();
    }


}
