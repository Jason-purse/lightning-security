package com.generatera.resource.server.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {
    private LogUtil() {

    }
    public static void prettyLog(String message) {
        log.info("###################################################################################################");
        log.info("{}",message);
        log.info("###################################################################################################");
    }
}
