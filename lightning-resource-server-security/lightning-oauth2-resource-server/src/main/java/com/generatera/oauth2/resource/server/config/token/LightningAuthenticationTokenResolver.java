package com.generatera.oauth2.resource.server.config.token;

import javax.servlet.http.HttpServletRequest;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 13:14
 * @Description token 解析器 ..
 *
 * 约定,不需要重写 resolve 方法, 或者必须调用 super.resolve 方法写入当前访问token
 */
@FunctionalInterface
public interface LightningAuthenticationTokenResolver {

    public static final String TOKEN_IDENTITY_NAME = "Authorization";


    default String resolve(HttpServletRequest request) {
        String s = doResolve(request);
        DefaultLightningAuthenticationTokenResolverHelper.setCurrentAccessToken(s);
        return s;
    }

    String doResolve(HttpServletRequest request);


    static String getCurrentAccessToken() {
        return DefaultLightningAuthenticationTokenResolverHelper.getCurrentAccessToken();
    }
}

class DefaultLightningAuthenticationTokenResolverHelper {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static String getCurrentAccessToken() {
        return threadLocal.get();
    }

    static void setCurrentAccessToken(String token) {
        threadLocal.set(token);
    }
}