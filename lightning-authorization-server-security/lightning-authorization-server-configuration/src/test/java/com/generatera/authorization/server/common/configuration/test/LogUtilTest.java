package com.generatera.authorization.server.common.configuration.test;

import com.generatera.security.authorization.server.specification.util.LogUtil;

public class LogUtilTest {
    public static void main(String[] args) {
        LogUtil.prettyLog("",new UnsupportedOperationException("error"));
    }
}
