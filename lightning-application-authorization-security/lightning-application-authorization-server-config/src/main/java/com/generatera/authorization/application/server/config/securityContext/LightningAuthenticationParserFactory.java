package com.generatera.authorization.application.server.config.securityContext;

import com.generatera.authorization.application.server.config.exception.ApplicationAuthorizationServerException;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 11:19
 * @Description 认证解析器认证工厂
 */
public class LightningAuthenticationParserFactory {

    private static final Map<Class<? extends LightningAuthenticationParser>, LightningAuthenticationParser> factory =
            new ConcurrentHashMap<>();

    @NotNull
    public static LightningAuthenticationParser loadParser(Class<? extends LightningAuthenticationParser> clazz) {
        LightningAuthenticationParser lightningAuthenticationParser = factory.get(clazz);
        if(lightningAuthenticationParser != null) {
            return lightningAuthenticationParser;
        }
        throw ApplicationAuthorizationServerException.throwNoAuthenticationParserError();
    }

    @SuppressWarnings("unchecked")
    public static LightningAuthenticationParser loadParser(String parserClass) {
        try {
            Class<?> aClass = ClassUtils.forName(parserClass, LightningAuthenticationParserFactory.class.getClassLoader());
            return loadParser(((Class<LightningAuthenticationParser>) aClass));
        }catch (Exception e) {
            throw ApplicationAuthorizationServerException.throwNoAuthenticationParserError();
        }
    }

    public static void registerParser(Class<? extends LightningAuthenticationParser> clazz, LightningAuthenticationParser instance) {
        factory.put(clazz,instance);
    }
}
