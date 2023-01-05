package com.generatera.resource.server.specification.token.jwt.bearer;

import com.generatera.resource.server.config.token.LightningAuthenticationTokenResolver;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
/**
 * @author FLJ
 * @date 2023/1/5
 * @time 14:20
 * @Description 基于Header 进行BearerToken 解析,是 DefaultBearerTokenResolver的简化版本
 */
public class HeaderBearerTokenResolver implements LightningAuthenticationTokenResolver {
    private final String header;

    public HeaderBearerTokenResolver(String header) {
        Assert.hasText(header, "header cannot be empty");
        this.header = header;
    }

    public String resolve(HttpServletRequest request) {
        return request.getHeader(this.header);
    }
}