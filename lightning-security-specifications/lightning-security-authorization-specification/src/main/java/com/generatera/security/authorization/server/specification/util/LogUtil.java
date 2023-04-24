package com.generatera.security.authorization.server.specification.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {
    private static Boolean enableLog = Boolean.valueOf(System.getProperty("lightning.security.logging.enable","true"));
    private LogUtil() {

    }
    public static void prettyLog(String message) {
        logClaimAction(() -> {
            log.info("###################################################################################################");
            log.info("{}",message);
            log.info("###################################################################################################");
        });
        }

    public static void prettyLogWarning(String message) {
        logClaimAction(() -> {
            log.warn("###################################################################################################");
            log.warn("{}",message);
            log.warn("###################################################################################################");

        });
        }

    public static void prettyLog(String message,Throwable e) {

        logClaimAction(() -> {
            log.info("###################################################################################################");
            log.info("{}",message);
            log.info("cause: {}",e.getMessage());
            // 打印 堆栈 信息 ..
            e.printStackTrace();
            log.info("###################################################################################################");

        });
    }

    private static void logClaimAction(Execute execute) {
        if(enableLog) {
            execute.excute();
        }
    }

    interface Execute  {
        void excute();
    }
}
