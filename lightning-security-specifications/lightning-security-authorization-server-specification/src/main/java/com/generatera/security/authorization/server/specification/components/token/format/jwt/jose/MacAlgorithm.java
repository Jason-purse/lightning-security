package com.generatera.security.authorization.server.specification.components.token.format.jwt.jose;

import com.generatera.security.authorization.server.specification.JwsAlgorithm;

public enum MacAlgorithm implements JwsAlgorithm {
    HS256("HS256"),
    HS384("HS384"),
    HS512("HS512");

    private final String name;

    private MacAlgorithm(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static MacAlgorithm from(String name) {
        MacAlgorithm[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            MacAlgorithm algorithm = var1[var3];
            if (algorithm.getName().equals(name)) {
                return algorithm;
            }
        }

        return null;
    }
}