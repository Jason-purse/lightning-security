package com.generatera.security.authorization.server.specification.components.token.format.plain;

import org.springframework.util.AlternativeJdkIdGenerator;

public final class UuidUtil {
    private final static AlternativeJdkIdGenerator uuidGenerator = new AlternativeJdkIdGenerator();

    public static String nextId() {
        return uuidGenerator.generateId().toString();
    }
}
