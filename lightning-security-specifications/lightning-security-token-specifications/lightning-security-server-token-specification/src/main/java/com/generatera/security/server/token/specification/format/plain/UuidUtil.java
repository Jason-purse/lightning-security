package com.generatera.security.server.token.specification.format.plain;

import org.springframework.util.AlternativeJdkIdGenerator;

final class UuidUtil {
    private final static AlternativeJdkIdGenerator uuidGenerator = new AlternativeJdkIdGenerator();

    public static String nextId() {
        return uuidGenerator.generateId().toString();
    }
}
