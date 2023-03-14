package com.generatera.resource.server.config.method.security;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author FLJ
 * @date 2023/3/14
 * @time 10:45
 * @Description 资源行为
 */
public interface ResourceBehavior {

    public static final String WRITE = "write";

    public static final String READ = "read";

    public static final String WRITE_AND_READ = "write_and_read";


    public static void registerBehavior(String behavior) {
        ResourceBehaviorManager.register(behavior);
    }

    public static Collection<String> getBehaviors() {
        return ResourceBehaviorManager.getBehaviors();
    }
}

class ResourceBehaviorManager {
    private final static Set<String> behaviors = new LinkedHashSet<>();

    static {
        behaviors.add(ResourceBehavior.WRITE);
        behaviors.add(ResourceBehavior.READ);
        behaviors.add(ResourceBehavior.WRITE_AND_READ);
    }

    public static void register(String behavior) {
        behaviors.add(behavior);
    }

    public static Set<String> getBehaviors() {
        return Collections.unmodifiableSet(behaviors);
    }
}
