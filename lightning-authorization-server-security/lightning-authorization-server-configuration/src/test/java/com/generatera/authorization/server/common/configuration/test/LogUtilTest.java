package com.generatera.authorization.server.common.configuration.test;

import com.generatera.authorization.server.common.configuration.util.LogUtil;

public class LogUtilTest {
    public static void main(String[] args) {
        LogUtil.prettyLog("",new UnsupportedOperationException("error"));
    }
}
