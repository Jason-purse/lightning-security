package com.generatera.authorization.application.server.config.util;

public class StringUtils {
    public static String normalize(String path) {
        return normalize(path, true);
    }

    public static String normalize(String path, boolean needLeaf) {
        return "/" + org.springframework.util.StringUtils.trimTrailingCharacter(org.springframework.util.StringUtils.trimLeadingCharacter(path, '/'), '/') + (needLeaf ? "/" : "");

    }
}
