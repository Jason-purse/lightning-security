package com.generatera.authorization.application.server.config;

import org.springframework.core.log.LogMessage;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * @author FLJ
 * @date 2023/1/30
 * @time 11:19
 * @Description 自己的logout page geneartor
 */
public class MyDefaultLogoutPageGeneratingFilter extends DefaultLogoutPageGeneratingFilter {

    private RequestMatcher matcher = new AntPathRequestMatcher("/logout", "GET");

    private String logoutProcessUrl = "/logout";

    /**
     * 在 非分离的情况下,需要获取 token ...
     */
    private Function<HttpServletRequest, Map<String, String>> resolveHiddenInputs = (request) -> {
        CsrfToken token = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
        return token != null ? Collections.singletonMap(token.getParameterName(), token.getToken()) : Collections.emptyMap();
    };


    public MyDefaultLogoutPageGeneratingFilter() {
    }

    public void setMatcher(RequestMatcher matcher) {
        Assert.notNull(matcher, "matcher must not be null !!!");
        this.matcher = matcher;
    }

    public void setLogoutProcessUrl(String logoutProcessUrl) {
        Assert.notNull(logoutProcessUrl, "logoutProcessUrl must not be null !!!");
        this.logoutProcessUrl = logoutProcessUrl;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.matcher.matches(request)) {
            this.renderLogout(request, response);
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not render default logout page since request did not match [%s]", this.matcher));
            }

            filterChain.doFilter(request, response);
        }

    }

    private void renderLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("  <head>\n");
        sb.append("    <meta charset=\"utf-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n");
        sb.append("    <meta name=\"description\" content=\"\">\n");
        sb.append("    <meta name=\"author\" content=\"\">\n");
        sb.append("    <title>Confirm Log Out?</title>\n");
        sb.append("    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">\n");
        sb.append("    <link href=\"https://getbootstrap.com/docs/4.0/examples/signin/signin.css\" rel=\"stylesheet\" crossorigin=\"anonymous\"/>\n");
        sb.append("  </head>\n");
        sb.append("  <body>\n");
        sb.append("     <div class=\"container\">\n");
        sb.append("      <form class=\"form-signin\" method=\"post\" action=\"" + logoutProcessUrl +"\"" + "\n");
        sb.append("        <h2 class=\"form-signin-heading\">Are you sure you want to log out?</h2>\n");
        sb.append(this.renderHiddenInputs(request) + "        <button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Log Out</button>\n");
        sb.append("      </form>\n");
        sb.append("    </div>\n");
        sb.append("  </body>\n");
        sb.append("</html>");
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(sb.toString());
    }

    public void setResolveHiddenInputs(Function<HttpServletRequest, Map<String, String>> resolveHiddenInputs) {
        Assert.notNull(resolveHiddenInputs, "resolveHiddenInputs cannot be null");
        this.resolveHiddenInputs = resolveHiddenInputs;
    }

    private String renderHiddenInputs(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Iterator var3 = ((Map) this.resolveHiddenInputs.apply(request)).entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<String, String> input = (Map.Entry) var3.next();
            sb.append("<input name=\"");
            sb.append((String) input.getKey());
            sb.append("\" type=\"hidden\" value=\"");
            sb.append((String) input.getValue());
            sb.append("\" />\n");
        }

        return sb.toString();
    }
}
