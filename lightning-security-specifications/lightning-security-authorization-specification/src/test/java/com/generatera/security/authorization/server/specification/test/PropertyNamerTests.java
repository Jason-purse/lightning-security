package com.generatera.security.authorization.server.specification.test;

import com.jianyue.lightning.boot.starter.util.lambda.PropertyNamer;
import org.junit.jupiter.api.Test;

public class PropertyNamerTests {
    @Test
    public void test() {
        System.out.println(PropertyNamer.methodToProperty("getValue"));
    }
}
