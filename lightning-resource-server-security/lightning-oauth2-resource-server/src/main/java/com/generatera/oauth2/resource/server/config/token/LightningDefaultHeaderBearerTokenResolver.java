package com.generatera.oauth2.resource.server.config.token;

import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * 仅仅解析 不包含空格的有效token
 */
public class LightningDefaultHeaderBearerTokenResolver extends HeaderBearerTokenResolver implements LightningAuthenticationTokenResolver {
    public LightningDefaultHeaderBearerTokenResolver(String header) {
        super(header);
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String resolve = super.resolve(request);

        // 有空格的不是有效token
        // // TODO: 2023/4/10  例如 basic token  / bearer token 无法识别 ..
        // 而仅仅只有 authorization: ..sdfs.d.f.  才是有效的业务token
        // 需要处理掉 ...
        if (resolve != null && resolve.contains(" ")) {
            return null;
        }
        return resolve;
    }

    @Override
    public String doResolve(HttpServletRequest request) {
        return resolve(request);
    }
}

