package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.util.HttpSecurityBuilderUtils;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author FLJ
 * @date 2023/2/6
 * @time 17:30
 * @Description 根据Context抽象获取 引导上下文中的内容 ...
 */
public interface BootstrapContext extends Context {

    void put(Object key, Object value);

    public static BootstrapContext of() {
        return new DefaultBootstrapContext();
    }

    public static <H extends HttpSecurityBuilder<H>> BootstrapContext fromHttpSecurity(H builder) {
        return HttpSecurityBuilderUtils.getBean(builder,BootstrapContext.class);
    }
}

class DefaultBootstrapContext implements BootstrapContext {

    private final Map<Object, Object> context = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(Object key) {
        return (V) context.get(key);
    }

    @Override
    public boolean hasKey(Object key) {
        return context.containsKey(key);
    }

    @Override
    public void put(Object key, Object value) {
        context.put(key, value);
    }
}
