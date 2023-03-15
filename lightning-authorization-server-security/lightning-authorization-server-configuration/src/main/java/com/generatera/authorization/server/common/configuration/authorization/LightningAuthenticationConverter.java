package com.generatera.authorization.server.common.configuration.authorization;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author FLJ
 * @date 2023/3/15
 * @time 11:45
 * @Description 将请求转换为各种未认证的 authentication, 然后被authenticationManager 进行认证 ..
 */
public interface LightningAuthenticationConverter {
    Authentication convert(HttpServletRequest request, HttpServletResponse response);
}
