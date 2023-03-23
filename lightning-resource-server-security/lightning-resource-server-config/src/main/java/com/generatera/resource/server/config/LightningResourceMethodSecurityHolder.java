package com.generatera.resource.server.config;

import com.jianyue.lightning.boot.starter.util.ThreadLocalSupport;
import com.jianyue.lightning.boot.starter.util.dataflow.Tuple4;

/**
 * @author FLJ
 * @date 2023/3/23
 * @time 11:06
 * @Description 当前进入的资源方法安全 holder
 */
public class LightningResourceMethodSecurityHolder {

    private LightningResourceMethodSecurityHolder() {

    }

    private final static ThreadLocalSupport<Tuple4<String,String,String,String>> preLocalSupport = ThreadLocalSupport.Companion.of();
    private final static ThreadLocalSupport<Tuple4<String,String,String,String>> postLocalSupport = ThreadLocalSupport.Companion.of();

    public static Tuple4<String,String,String,String> getPreResourceMethodSecurity() {
        return preLocalSupport.get();
    }

    public static Tuple4<String,String,String,String> setPreResourceMethodSecurity(Tuple4<String,String,String,String> newValue) {
        return preLocalSupport.setAndReturnOld(newValue);
    }

    public static Tuple4<String,String,String,String> removePreResourceMethodSecurity() {
        return preLocalSupport.removeAndReturnOld();
    }

    public static Tuple4<String,String,String,String> getPostResourceMethodSecurity() {
        return postLocalSupport.get();
    }

    public static Tuple4<String,String,String,String> setPostResourceMethodSecurity(Tuple4<String,String,String,String> newValue) {
        return postLocalSupport.setAndReturnOld(newValue);
    }

    public static Tuple4<String,String,String,String> removePostResourceMethodSecurity() {
        return postLocalSupport.removeAndReturnOld();
    }


}
