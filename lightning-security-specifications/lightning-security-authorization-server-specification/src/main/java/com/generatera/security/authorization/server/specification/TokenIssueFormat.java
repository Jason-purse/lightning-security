package com.generatera.security.authorization.server.specification;

import com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * @author FLJ
 * @date 2023/1/3
 * @time 14:08
 * @Description Token Issue Format
 *
 * 目前存在两种格式:
 * 1. 自签名形式
 *  其中自签名形式,通过{@link com.generatera.security.authorization.server.specification.components.token.format.jwt.LightningJwtGenerator}
 *  生成访问token ...
 *  并且返回形式并不是一个{@link com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken}
 *  但是它是一个 {@link com.generatera.security.authorization.server.specification.components.token.LightningToken.ComplexToken}
 *  所以如果想要获取一些信息,可以尝试转换为 ComplexToken ..
 *
 *  但是了解到这个缺陷之后,我们需要知道的是, token的生成工作是代理到{@link com.generatera.security.authorization.server.specification.components.token.LightningTokenGenerator}
 *  也就是根本不知道它生成的到底是一个什么的token,但是它token 值可以被对应的Token 类型所使用,所以我们需要根据最简单的Token去包装成对应的特定Token
 *  例如{@link com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningAccessToken} 或者
 *  {@link com.generatera.security.authorization.server.specification.components.token.LightningToken.LightningRefreshToken}
 *
 *  详情查看 {@link com.generatera.authorization.application.server.oauth2.login.config.authentication.LightningOAuth2LoginAuthenticationEntryPoint#onAuthenticationSuccess(HttpServletRequest, HttpServletResponse, Authentication)}
 *  以及 {@link LightningAccessTokenGenerator.LightningAuthenticationAccessToken}
 * 2. 引用形式(由其他CA机构签名的),但是不一定是可信赖的 ...
 * 本质上是基于{@link com.generatera.security.authorization.server.specification.components.token.LightningAccessTokenGenerator}
 * 进行访问token生成
 *
 * @see com.generatera.security.authorization.server.specification.components.token.format.jwt.DefaultLightningJwtGenerator
 * @see com.generatera.security.authorization.server.specification.components.token.DefaultLightningAccessTokenGenerator
 * @see com.generatera.security.authorization.server.specification.components.token.format.jwt.jose.NimbusJwtEncoder
 */
public final class TokenIssueFormat implements Serializable {
    public static final TokenIssueFormat SELF_CONTAINED;
    public static final TokenIssueFormat REFERENCE;
    private final String value;

    public TokenIssueFormat(String value) {
        Assert.hasText(value, "value cannot be empty");
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            TokenIssueFormat that = (TokenIssueFormat)obj;
            return this.getValue().equals(that.getValue());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.getValue().hashCode();
    }

    static {
        SELF_CONTAINED = new TokenIssueFormat("self-contained");
        REFERENCE = new TokenIssueFormat("reference");
    }
}