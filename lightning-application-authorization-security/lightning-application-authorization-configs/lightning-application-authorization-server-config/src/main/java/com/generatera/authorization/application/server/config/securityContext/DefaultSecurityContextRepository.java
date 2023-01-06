package com.generatera.authorization.application.server.config.securityContext;

import com.generatera.security.authorization.server.specification.authentication.LightningSecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author FLJ
 * @date 2023/1/5
 * @time 9:50
 * @Description 保存 SecurityContext 仓库
 *
 * 内存型仓库 ..
 */
public class DefaultSecurityContextRepository implements LightningSecurityContextRepository {


    private final  Map<String,SecurityContext> cache = new ConcurrentHashMap<>();


    @Override
    public SecurityContext internalLoadContext(HttpServletRequest request) {
        return parseToken(request).map(cache::get).orElse(SecurityContextHolder.createEmptyContext());
    }

    private Optional<String> parseToken(HttpServletRequest request) {
        String header = request.getHeader(TOKEN_HEADER);
        if(StringUtils.hasText(header)) {
            return Optional.of(header);
        }
        return Optional.empty();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        //Object attribute = request.getAttribute(LightningAuthenticationToken.TOKEN_REQUEST_ATTRIBUTE);
        //if(!ObjectUtils.isEmpty(attribute)) {
        //    cache.put(attribute.toString(),context);
        //}
        //throw ApplicationAuthorizationServerException.throwNoTokenForSaveSecurityContextError();
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return parseToken(request).isPresent();
    }
}
