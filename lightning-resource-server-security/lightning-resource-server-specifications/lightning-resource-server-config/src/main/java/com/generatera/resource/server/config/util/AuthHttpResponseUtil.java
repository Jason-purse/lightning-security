package com.generatera.resource.server.config.util;

import org.springframework.http.MediaType;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 用于直接写出http response,但是我们需要注意 utf-8编码
 */
public class AuthHttpResponseUtil {

    private AuthHttpResponseUtil() {

    }

    public static void commence(HttpServletResponse response,String json) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // auto close ..
        try(ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e) {
            // catch exception
        }
    }
}
