package com.generata.lightning.application.authorization.oauth2.login.client.server.test;

import java.util.Objects;

public class ClassTests {


    public ClassTests() {
        System.out.println("构造器代码块");
    }
    {
        System.out.println("构造器代码块之后执行");
    }

    public final String value = Objects.requireNonNullElseGet(null,() -> {
        System.out.println("执行");
        return "1231";
    });

    public static void main(String[] args) {
        new ClassTests();
    }
}
