package com.generatera.resource.server.config.bootstrap;

import com.generatera.resource.server.config.token.LightningTokenAuthenticationFilter;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import java.util.Arrays;
import java.util.Collections;

public final class DefaultResourceServerConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<DefaultResourceServerConfigurer<H>, H> {

    private LightningTokenAuthenticationFilter tokenAuthenticationFilter;


    public DefaultResourceServerConfigurer<H> tokenAuthenticationFilter(LightningTokenAuthenticationFilter authenticationFilter) {
        Assert.notNull(authenticationFilter,"authenticationFilter cannot be null");
        this.tokenAuthenticationFilter = authenticationFilter;
        return this;
    }



    public void configure(H http) {
        tokenAuthenticationFilter = this.postProcess(tokenAuthenticationFilter);
        // 必然需要在持久化之前处理 ..
        http.addFilterBefore(tokenAuthenticationFilter, SecurityContextPersistenceFilter.class);
    }



    private void registerDefaultEntryPoint(H http) {
        @SuppressWarnings("unchecked")
        ExceptionHandlingConfigurer<H> exceptionHandling = (ExceptionHandlingConfigurer<H>) http.getConfigurer(ExceptionHandlingConfigurer.class);
        if (exceptionHandling != null) {
            ContentNegotiationStrategy contentNegotiationStrategy = http.getSharedObject(ContentNegotiationStrategy.class);
            if (contentNegotiationStrategy == null) {
                contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
            }

            MediaTypeRequestMatcher restMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy,
                    MediaType.APPLICATION_ATOM_XML,
                    MediaType.APPLICATION_FORM_URLENCODED,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_OCTET_STREAM,
                    MediaType.APPLICATION_XML,
                    MediaType.MULTIPART_FORM_DATA,
                    MediaType.TEXT_XML);

            restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
            MediaTypeRequestMatcher allMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.ALL);
            allMatcher.setUseEquals(true);
            RequestMatcher notHtmlMatcher = new NegatedRequestMatcher(new MediaTypeRequestMatcher(contentNegotiationStrategy, MediaType.TEXT_HTML));
            RequestMatcher restNotHtmlMatcher = new AndRequestMatcher(Arrays.asList(notHtmlMatcher, restMatcher));
            //RequestMatcher preferredMatcher = new OrRequestMatcher(Arrays.asList(this.requestMatcher, X_REQUESTED_WITH, restNotHtmlMatcher, allMatcher));
            //exceptionHandling.defaultAuthenticationEntryPointFor(this.authenticationEntryPoint, preferredMatcher);
        }

    }

    private void registerDefaultCsrfOverride(H http) {
        @SuppressWarnings("unchecked")
        CsrfConfigurer<H> csrf = (CsrfConfigurer<H>) http.getConfigurer(CsrfConfigurer.class);
        if (csrf != null) {
            //csrf.ignoringRequestMatchers(this.requestMatcher);
        }

    }

}
